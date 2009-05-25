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

import it.could.confluence.autoexport.ExportManager;
import it.could.confluence.localization.LocalizedComponent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostCreateEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostRemoveEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostUpdateEvent;
import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageRemoveEvent;
import com.atlassian.confluence.event.events.content.page.PageUpdateEvent;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.BlogPost;
import com.atlassian.confluence.pages.Page;
import com.atlassian.event.Event;
import com.atlassian.event.EventListener;

/**
 * <p>A Confluence {@link EventListener} instance triggering exports and
 * deletions in the {@link ExportManager}.</p>
 */
public class ExportListener extends LocalizedComponent implements EventListener {

    /** <p>The array of events this instance can handle.</p> */
    private static final Class HANDLED_EVENTS[] = new Class[] {
                            PageCreateEvent.class, BlogPostCreateEvent.class,
                            PageRemoveEvent.class, BlogPostRemoveEvent.class,
                            PageUpdateEvent.class, BlogPostUpdateEvent.class };

    /** <p>A null {@link Notifiable} used by this implementation.</p> */
    private static final Notifiable NULL_NOTIFIABLE = new Notifiable() {
        public void notify(Object object) { }
    };

    /** <p>A {@link Map} used to hack infra-space moves of {@link Page}s.</p> */
    private final Map hack = new HashMap();

    /** <p>The {@link ExportManager} used by this instance.</p> */
    private final ExportManager exportManager;

    /**
     * <p>Create a new {@link ExportListener} instance.</p>
     * @param exportManager
     */
    public ExportListener(ExportManager exportManager) {
        this.exportManager = exportManager;
        this.log.info("Instance created");
    }

    /* ====================================================================== */
    /* EVENT HANDLING METHODS                                                 */
    /* ====================================================================== */

    /**
     * <p>Handle a {@link ConfluenceEvent} triggering export and remove
     * operations in the {@link ExportManager}.</p>
     */
    public void handleEvent(Event event) {
        if (event instanceof PageCreateEvent) {
            final PageCreateEvent pageEvent = (PageCreateEvent) event;
            this.export(pageEvent.getPage());

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
                this.remove(spaceKey, pageTitle, null);

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
                this.remove(spaceKey, pageTitle, null);
            }

            /* In any case, an update event means that we have to regenerate */
            this.export(page);

        } else if (event instanceof PageRemoveEvent) {
            final PageRemoveEvent pageEvent = (PageRemoveEvent) event;
            this.remove(pageEvent.getPage());
        
        } else if (event instanceof BlogPostCreateEvent) {
            final BlogPostCreateEvent blogEvent = (BlogPostCreateEvent) event;
            this.export(blogEvent.getBlogPost());

        } else if (event instanceof BlogPostUpdateEvent) {
            final BlogPostUpdateEvent blogEvent = (BlogPostUpdateEvent) event;
            this.remove(blogEvent.getBlogPost());
            this.export(blogEvent.getBlogPost());

        } else if (event instanceof BlogPostRemoveEvent) {
            final BlogPostRemoveEvent blogEvent = (BlogPostRemoveEvent) event;
            this.remove(blogEvent.getBlogPost());
        }
    }

    /**
     * <p>Export a {@link Page}, all its parents and finally the resources
     * associated with the specified {@link Page}'s space.</p>
     */
    private void export(Page page) {
        if (page == null) return;

        Page parent = page.getParent();
        while (parent != null) {
            this.exportManager.export(parent, NULL_NOTIFIABLE);
            parent = parent.getParent();
        }

        this.exportManager.export(page, NULL_NOTIFIABLE);
        this.exportManager.export(page.getSpace(), NULL_NOTIFIABLE, false);
    }

    /**
     * <p>Export a {@link BlogPost} and the resources associated with the
     * specified {@link BlogPost}'s space.</p>
     */
    private void export(BlogPost post) {
        if (post == null) return;
        this.exportManager.export(post, NULL_NOTIFIABLE);
        this.exportManager.export(post.getSpace(), NULL_NOTIFIABLE, false);
    }

    /**
     * <p>Remove a {@link Page}, and re-export all its parents and the resources
     * associated with the specified {@link Page}'s space.</p>
     */
    private void remove(Page page) {
        if (page == null) return;

        Page parent = page.getParent();
        while (parent != null) {
            this.exportManager.export(parent, NULL_NOTIFIABLE);
            parent = parent.getParent();
        }

        this.remove(page.getSpaceKey(), page.getTitle(), null);
        this.exportManager.export(page.getSpace(), NULL_NOTIFIABLE, false);
    }

    /**
     * <p>Remove a {@link BlogPost} and re-export the resources associated with
     * the specified {@link BlogPost}'s space.</p>
     */
    private void remove(BlogPost post) {
        if (post == null) return;
        this.remove(post.getSpaceKey(), post.getTitle(), post.getCreationDate());
        this.exportManager.export(post.getSpace(), NULL_NOTIFIABLE, false);
    }

    private void remove(String spaceKey, String pageTitle, Date postingDate) {
        // TODO: implements removal
    }

    /**
     * <p>Return an array of {@link ConfluenceEvent} {@link Class}es that this
     * {@link EventListener} can handle.</p>
     */
    public Class[] getHandledEventClasses() {
        return HANDLED_EVENTS;
    }

}
