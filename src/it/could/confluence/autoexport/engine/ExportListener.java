package it.could.confluence.autoexport.engine;

import it.could.confluence.ComponentSupport;

import java.util.HashMap;
import java.util.Map;

import bucket.container.ContainerManager;

import com.atlassian.confluence.event.EventListener;
import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageRemoveEvent;
import com.atlassian.confluence.event.events.content.page.PageUpdateEvent;
import com.atlassian.confluence.event.events.space.SpaceCreateEvent;
import com.atlassian.confluence.event.events.space.SpaceRemoveEvent;
import com.atlassian.confluence.event.events.space.SpaceUpdateEvent;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.Page;

public class ExportListener extends ComponentSupport implements EventListener {
    
    private static final Class HANDLED_EVENTS[] = new Class[] {
        PageCreateEvent.class,  PageRemoveEvent.class,  PageUpdateEvent.class,
        SpaceCreateEvent.class, SpaceRemoveEvent.class, SpaceUpdateEvent.class
    };

    private final Map hack = new HashMap(); 
    private final ExportEngine engine = new ExportEngine();
    private final Notifiable notifiable = new Notifiable() {
        public void notify(Object object) { }
    };

    public ExportListener() {
        ContainerManager.autowireComponent(this);
    }

    public void handleEvent(ConfluenceEvent event) {
        if (event instanceof PageCreateEvent) {
            final PageCreateEvent pageEvent = (PageCreateEvent) event;
            final Page page = pageEvent.getPage();

            this.engine.export(page, this.notifiable);

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

                this.log.info("Should delete page \"" + pageTitle
                              + "\" in space \"" + spaceKey
                              + "\" for update event [id="
                              + previous.getId() + "]");
                // TODO: remove the previous page.

            } else {
                final Long identifier = new Long(page.getId());
                final String spaceKey = (String) this.hack.remove(identifier);

                this.log.info("Should delete page \"" + page.getTitle()
                              + "\" in space \"" + spaceKey
                              + "\" for update event [id="
                              + identifier.toString() + "] (page moved)");
                // TODO: remove the previous page.
            }

            this.log.info("Exporting page \"" + page.getTitle()
                          + "\" in space \"" + page.getSpaceKey()
                          + "\" for update event [id=" + page.getId() + "]");
            this.engine.export(pageEvent.getPage(), this.notifiable);

        } else if (event instanceof PageRemoveEvent) {
            final PageRemoveEvent pageEvent = (PageRemoveEvent) event;

            final Page page = pageEvent.getPage();
            final String spaceKey = page.getSpaceKey();
            final String pageTitle = page.getTitle();
            this.log.info("Should delete page \"" + pageTitle + "\" in space \""
                          + spaceKey + "\" for remove event");
            // TODO: remove the page.
        }
        
        // TODO: handle space events.
    }

    public Class[] getHandledEventClasses() {
        return HANDLED_EVENTS;
    }

}
