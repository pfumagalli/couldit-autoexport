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
package it.could.confluence.autoexport;

import it.could.confluence.autoexport.engine.ExportUtils;
import it.could.confluence.autoexport.engine.ExportBeautifier;
import it.could.confluence.autoexport.engine.Notifiable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import bucket.container.ContainerManager;
import bucket.util.FileUtils;

import com.atlassian.confluence.core.actions.StylesheetAction;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.BlogPost;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.pages.actions.ViewPageAction;
import com.atlassian.confluence.pages.thumbnail.ThumbnailManager;
import com.atlassian.confluence.renderer.WikiStyleRenderer;
import com.atlassian.confluence.setup.BootstrapManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.util.GeneralUtil;
import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.ActionContext;

/**
 * <p>The {@link ExportManager} class represents the core object exporting
 * content out of Confluence.</p>
 */
public class ExportManager extends ComponentSupport {
    
    /** <p>The key for the request in the current {@link ActionContext}.</p> */ 
    private static final String AC_REQUEST_KEY =
                        "com.opensymphony.xwork.dispatcher.HttpServletRequest";

    /** <p>The {@link LocationManager} used by this instance.</p> */
    private final LocationManager locationManager;
    /** <p>The {@link BootstrapManager} used by this instance.</p> */
    private BootstrapManager bootstrapManager = null;
    /** <p>The {@link PageManager} used by this instance.</p> */
    private PageManager pageManager = null;
    /** <p>The {@link SpaceManager} used by this instance.</p> */
    private SpaceManager spaceManager = null;
    /** <p>The {@link ThumbnailManager} used by this instance.</p> */
    private ThumbnailManager thumbnailManager = null;
    /** <p>The {@link WikiStyleRenderer} rendering content.</p> */
    private WikiStyleRenderer wikiStyleRenderer = null;

    /** <p>Deny public construction.</p> */
    ExportManager(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    /* ====================================================================== */
    /* SPRING AUTO-WIRING SETTERS                                             */
    /* ====================================================================== */

    /**
     * <p>Setter for Spring's component wiring.</p>
     */
    public void setBootstrapManager(BootstrapManager bootstrapManager) {
        this.bootstrapManager = bootstrapManager;
    }

    /**
     * <p>Setter for Spring's component wiring.</p>
     */
    public void setSpaceManager(SpaceManager spaceManager) {
        this.spaceManager = spaceManager;
    }
    
    /**
     * <p>Setter for Spring's component wiring.</p>
     */
    public void setPageManager(PageManager pageManager) {
        this.pageManager = pageManager;
    }

    /**
     * <p>Setter for Spring's component wiring.</p>
     */
    public void setThumbnailManager(ThumbnailManager thumbnailManager) {
        this.thumbnailManager = thumbnailManager;
    }

    /**
     * <p>Setter for Spring's component wiring.</p>
     */
    public void setWikiStyleRenderer(WikiStyleRenderer wikiStyleRenderer) {
        this.wikiStyleRenderer = wikiStyleRenderer;
    }

    /* ====================================================================== */
    /* MAIN METHODS FOR CONTENT EXPORT                                        */
    /* ====================================================================== */

    /**
     * <p>Export all the content from all specified spaces.</p>
     */
    public void export(String spaceKeys[],
                       Notifiable notifiable,
                       boolean exportPages) {
        if (spaceKeys == null) return;
        for (int x = 0; x < spaceKeys.length; x ++) {
            this.export(spaceKeys[x], notifiable, exportPages);
        }
    }

    /**
     * <p>Export all the content from the specified space.</p>
     */
    public void export(String spaceKey,
                       Notifiable notifiable,
                       boolean exportPages) {
        if (spaceKey == null) return;
        final Space space = this.spaceManager.getSpace(spaceKey);
        this.export(space, notifiable, exportPages);
    }

    /**
     * <p>Export all the content from the specified space.</p>
     */
    public void export(Space space,
                       Notifiable notifiable,
                       boolean exportPages) {

        /* If the user does not have permission to export the space, do nothing */
        if (space == null) return;
        if (! this.locationManager.exportable(space)) {
            this.message(notifiable, "msg.locked-space", space, null, null);
            return;
        }

        /* Start exporting the space: pages resources and all */
        this.debug("msg.exporting-space", space, null, null);

        if (exportPages) {
            final List pagesList = this.spaceManager.getPages(space, true);
            final Iterator pages = pagesList.iterator();
            while (pages.hasNext()) {
                this.export((Page) pages.next(), notifiable);
            }
            final List postsList = this.spaceManager.getBlogPosts(space, true);
            final Iterator posts = postsList.iterator();
            while (posts.hasNext()) {
                this.export((BlogPost) posts.next(), notifiable);
            }
        }

        /* Export the resources associated with the space */
        final String styleData = StylesheetAction.renderSpaceStylesheet(space);
        final File styleFile = this.locationManager.getFile(space, "space.css");
        try {
            final File resourcesDir = styleFile.getParentFile();
            if (! resourcesDir.isDirectory()) resourcesDir.mkdirs();
            FileOutputStream stream = new FileOutputStream(styleFile);
            OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
            writer.write(styleData);
            writer.flush();
            stream.flush();
            writer.close();
            stream.close();
        } catch (IOException exception) {
            this.error(notifiable, exception, "err.stylesheet", space, null, styleFile);
        }

        this.message(notifiable, "msg.exported-space", space, null, null);
    }

    /**
     * <p>Export the specified page.</p>
     */
    public void export(AbstractPage page, Notifiable notifiable) {
        /* If the user does not have permission to export the page, do nothing */
        if (page == null) return;
        if (! this.locationManager.exportable(page)) {
            this.message(notifiable, "msg.locked-page", null, page, null);
            return;
        }

        this.debug("msg.exporting-page", null, page, null);

        try {
            final VelocityContext context = new VelocityContext();
            final Template template = this.templatesManager.getTemplate(page.getSpaceKey());
            final String body = this.wikiStyleRenderer.convertWikiToXHtml(page.toPageContext(), page.getContent());
            final String styleUri = this.locationManager.getLocation(page.getSpace(), "space.css").toString();
            final String confluenceUri = this.bootstrapManager.getBaseUrl();
            final Object action = new ViewPageAction();
            ContainerManager.autowireComponent(action);

            context.put("generalUtil", new GeneralUtil());
            context.put("webwork", new TextUtils());
            context.put("autoexport", new ExportUtils());
            context.put("confluenceUri", confluenceUri);
            context.put("stylesheet", styleUri);
            context.put("action", action);
            context.put("page", page);
            context.put("body", body);
            context.put("req", ActionContext.getContext().get(AC_REQUEST_KEY));
            final File pageFile = this.locationManager.getFile(page);
            final File spaceDir = pageFile.getParentFile();
            if (! spaceDir.isDirectory()) spaceDir.mkdirs();
            try {
                final StringWriter writer = new StringWriter();
                template.merge(context, writer);
                writer.flush();
                writer.close();
                final ExportBeautifier beautifier = new ExportBeautifier(page,
                                        this.configurationManager.getEncoding(),
                                        this.pageManager, this.spaceManager,
                                        this.locationManager, this.bootstrapManager);
                beautifier.beautify(writer.toString(), pageFile);
            } catch (Exception exception) {
                this.error(notifiable, exception, "err.exporting-page", null, page, null);
            }
            final Iterator iterator = page.getAttachments().iterator();

            while (iterator.hasNext()) {
                final Attachment attachment = (Attachment) iterator.next();

                final File aFile = this.locationManager.getFile(attachment, false);

                final File aDir = aFile.getParentFile();
                if (! aDir.isDirectory()) aDir.mkdirs();

                try {
                    final InputStream aInput = attachment.getContentsAsStream();
                    FileUtils.copyFile(aInput, aFile, true);
                    aInput.close();
                    this.debug("msg.exported-attachment", null, page, aFile.getName());

                } catch (IOException exception) {
                    this.error(notifiable, exception, "err.exporting-attachment",
                               null, page, aFile.getName());
                }

                /* We need to verify if the thumbnail exists or not */
                if (! this.thumbnailManager.isThumbnailable(attachment)) continue;

                /* This will create the thumbnail on disk, if it doesn't exist */
                this.thumbnailManager.getThumbnail(attachment);

                /* Now export the thumbnail normally */
                final File sFile = this.thumbnailManager.getThumbnailFile(attachment);
                if (sFile.exists()) try {
                    final File tFile = this.locationManager.getFile(attachment, true);
                    final File tDir = tFile.getParentFile();
                    if (! tDir.isDirectory()) tDir.mkdirs();
                    if (tFile.exists()) tFile.delete();
                    FileUtils.copyFile(sFile, tFile);
                    this.debug("msg.exported-thumbnail", null, page, tFile.getName());

                } catch (IOException exception) {
                    this.error(notifiable, exception, "err.exporting-thumbnail",
                               null, page, aFile.getName());
                }
            }

        } catch (Exception exception) {
            this.error(notifiable, exception, "err.exporting-page", null, page, null);
        }

        this.message(notifiable, "msg.exported-page", null, page, null);
    }

    /* ====================================================================== */
    /* PRIVATE METHODS FOR ERROR AND MESSAGES NOTIFICATION                    */
    /* ====================================================================== */
    
    private Object[] getParams(Space space, AbstractPage page, Object arg) {
        if (page == null) {
            final String sname = space == null ? null : space.getName();
            return new Object[] { null, sname, arg };
        } else if (page instanceof BlogPost) {
            final Space sinst = space == null ? page.getSpace() : space;
            final String sname = sinst == null ? null : sinst.getName();
            final Date date = page.getCreationDate();
            final StringBuffer buffer = new StringBuffer();
            buffer.append(new SimpleDateFormat("yyyy/MM/dd: ").format(date));
            buffer.append(page.getTitle());
            return new Object[] { buffer.toString(), sname, arg };
        } else {
            final Space sinst = space == null ? page.getSpace() : space;
            final String sname = sinst == null ? null : sinst.getName();
            return new Object[] { page.getTitle(), sname, arg };
        }
    }

    private void error(Notifiable notifiable, Throwable exception, String key,
                       Space space, AbstractPage page, Object arg) {
        final Object params[] = this.getParams(space, page, arg);
        final String message = this.localizeMessage(key, params);
        if (notifiable != null) {
            notifiable.notify(message);
            notifiable.notify(exception);
        }
        this.log.warn(message, exception);
    }

    private void message(Notifiable notifiable, String key, Space space,
                         AbstractPage page, Object arg) {
        final Object params[] = this.getParams(space, page, arg);
        final String message = this.localizeMessage(key, params);
        if (notifiable != null) notifiable.notify(message);
        this.log.info(message);
    }

    private void debug(String key, Space space, AbstractPage page, Object arg) {
        final Object params[] = this.getParams(space, page, arg);
        final String message = this.localizeMessage(key, params);
        this.log.debug(message);
    }
}
