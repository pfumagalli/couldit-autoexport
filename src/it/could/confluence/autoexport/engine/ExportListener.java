/* ========================================================================== *
 *   Copyright (c) 2006, Pier Paolo Fumagalli <mailto:pier@betaversion.org>   *
 *                            All rights reserved.                            *
 * ========================================================================== *
 *                                                                            * 
 * Redistribution and use in source and binary forms, with or without modifi- *
 * cation, are permitted provided that the following conditions are met:      *
 *                                                                            * 
 *  - Redistributions of source code must retain the  above copyright notice, *
 *    this list of conditions and the following disclaimer.                   *
 *                                                                            * 
 *  - Redistributions  in binary  form  must  reproduce the  above  copyright *
 *    notice,  this list of conditions  and the following  disclaimer  in the *
 *    documentation and/or other materials provided with the distribution.    *
 *                                                                            * 
 *  - Neither the name of Pier Fumagalli, nor the names of other contributors *
 *    may be used to endorse  or promote products derived  from this software *
 *    without specific prior written permission.                              *
 *                                                                            * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" *
 * AND ANY EXPRESS OR IMPLIED WARRANTIES,  INCLUDING, BUT NOT LIMITED TO, THE *
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE *
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER  OR CONTRIBUTORS BE *
 * LIABLE  FOR ANY  DIRECT,  INDIRECT,  INCIDENTAL,  SPECIAL,  EXEMPLARY,  OR *
 * CONSEQUENTIAL  DAMAGES  (INCLUDING,  BUT  NOT LIMITED  TO,  PROCUREMENT OF *
 * SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS;  OR BUSINESS *
 * INTERRUPTION)  HOWEVER CAUSED AND ON  ANY THEORY OF LIABILITY,  WHETHER IN *
 * CONTRACT,  STRICT LIABILITY,  OR TORT  (INCLUDING NEGLIGENCE OR OTHERWISE) *
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE *
 * POSSIBILITY OF SUCH DAMAGE.                                                *
 * ========================================================================== */
package it.could.confluence.autoexport.engine;

import it.could.confluence.ComponentSupport;

import java.util.HashMap;
import java.util.Map;

import bucket.container.ContainerManager;

import com.atlassian.confluence.event.EventListener;
import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostCreateEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostRemoveEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostUpdateEvent;
import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageRemoveEvent;
import com.atlassian.confluence.event.events.content.page.PageUpdateEvent;
import com.atlassian.confluence.event.events.space.SpaceCreateEvent;
import com.atlassian.confluence.event.events.space.SpaceRemoveEvent;
import com.atlassian.confluence.event.events.space.SpaceUpdateEvent;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.Page;

/**
 * <p>A Confluence {@link EventListener} instance triggering exports and
 * deletions in the {@link ExportEngine}.</p>
 */
public class ExportListener extends ComponentSupport implements EventListener {

    /** <p>The array of events this instance can handle.</p> */
    private static final Class HANDLED_EVENTS[] = new Class[] {
        PageCreateEvent.class,     PageRemoveEvent.class,     PageUpdateEvent.class,
        BlogPostCreateEvent.class, BlogPostRemoveEvent.class, BlogPostUpdateEvent.class,
        SpaceCreateEvent.class,    SpaceRemoveEvent.class,    SpaceUpdateEvent.class
    };

    /** <p>A null {@link Notifiable} used by this implementation.</p> */
    private static final Notifiable NULL_NOTIFIABLE = new Notifiable() {
        public void notify(Object object) { }
    };

    /** <p>A {@link Map} used to hack infra-space moves of {@link Page}s.</p> */
    private final Map hack = new HashMap();
    /** <p>The {@link ExportEngine} used by this instance.</p> */
    private final ExportEngine enginex = new ExportEngine();

    /**
     * <p>Create a new {@link ExportListener} instance.</p>
     */
    public ExportListener() {
        ContainerManager.autowireComponent(this);
    }

    /**
     * <p>Handle a {@link ConfluenceEvent} triggering export and remove
     * operations in the {@link ExportEngine}.</p>
     */
    public void handleEvent(ConfluenceEvent event) {
        if (event instanceof PageCreateEvent) {
            final PageCreateEvent pageEvent = (PageCreateEvent) event;
            final Page page = pageEvent.getPage();
            this.export(page);

        } else if (event instanceof PageUpdateEvent) {
            final PageUpdateEvent pageEvent = (PageUpdateEvent) event;
            final AbstractPage previous = pageEvent.getOriginalPage();
            final Page page = pageEvent.getPage();

            /*
             * Let's try to hack into Confluence to figure out from previous
             * events in what space the page being updated used to reside:
             * Confluence sends two events when a page is moved from one space
             * to another: a first one with the page updated in the old space
             * (with id X) and a second one where the original page is null,
             * but the same id X of the previous event.
             */
            if (previous != null) {
                /* The previous page is not null, thus the page was not moved */
                final String spaceKey = previous.getSpaceKey() == null ?
                                        page.getSpaceKey():
                                        previous.getSpaceKey();
                final String pageTitle = previous.getTitle();

                /* We replace the old space id with the new one */
                this.hack.remove(new Long(previous.getId()));
                this.hack.put(new Long(page.getId()), page.getSpaceKey());
                
                /* Remove the previous page */
                this.remove(spaceKey, pageTitle);

            } else {
                /*
                 * The previous page is null, this means that the page was moved
                 * to another space. In this case the old space is retrieved
                 * from our "hack" table, and we simply remove whatever we can
                 * find.
                 */
                final Long identifier = new Long(page.getId());
                final String spaceHack = (String) this.hack.remove(identifier);
                final String spaceKey = spaceHack != null ?
                                        spaceHack : page.getSpaceKey();
                final String pageTitle = page.getTitle();
                this.remove(spaceKey, pageTitle);
            }

            /* In any case, an update event means that we have to regenerate */
            this.export(page);

        } else if (event instanceof PageRemoveEvent) {
            final PageRemoveEvent pageEvent = (PageRemoveEvent) event;
            this.remove(pageEvent.getPage());
        }
        
        // TODO: handle space and blog events.
    }

    /**
     * <p>Export a {@link Page}, all its parents and finally the resources
     * associated with the specified {@link Page}'s space.</p>
     */
    private void export(Page page) {
        if (page == null) return;
        
        final Page parent = page.getParent();
        if (parent == null) {
            this.enginex.export(page.getSpaceKey(), NULL_NOTIFIABLE, false);
        } else {
            this.export(parent);
        }
        this.enginex.export(page, NULL_NOTIFIABLE);
    }

    /**
     * <p>Remove a {@link Page}, and re-export all its parents and the resources
     * associated with the specified {@link Page}'s space.</p>
     */
    private void remove(Page page) {
        if (page == null) return;

        final Page parent = page.getParent();
        if (parent == null) {
            this.enginex.export(page.getSpaceKey(), NULL_NOTIFIABLE, false);
        } else {
            this.export(parent);
        }
        this.remove(page.getSpaceKey(), page.getTitle());
    }

    /**
     * <p>Remove the page associated with the specified space key and 
     * title.</p>
     */
    private void remove(String spaceKey, String pageTitle) {
        this.enginex.remove(spaceKey, pageTitle, NULL_NOTIFIABLE);
    }

    /**
     * <p>Return an array of {@link ConfluenceEvent} {@link Class}es that this
     * {@link EventListener} can handle.</p>
     */
    public Class[] getHandledEventClasses() {
        return HANDLED_EVENTS;
    }

}
