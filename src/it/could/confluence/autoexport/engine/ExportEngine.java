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
import it.could.confluence.autoexport.ConfigurationManager;
import it.could.confluence.autoexport.TemplatesManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import bucket.container.ContainerManager;
import bucket.util.FileUtils;

import com.atlassian.confluence.core.actions.StylesheetAction;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.thumbnail.ThumbnailManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.util.GeneralUtil;
import com.atlassian.user.EntityException;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;

/**
 * <p>The {@link ExportEngine} class represents the core object exporting
 * content out of Confluence.</p>
 */
public class ExportEngine extends ComponentSupport {

    /** <p>The {@link TemplatesManager} used by this instance.</p> */
    private final TemplatesManager templatesManager = TemplatesManager.INSTANCE;

    /** <p>The {@link SpaceManager} used by this instance.</p> */
    private SpaceManager spaceManager = null;
    /** <p>The {@link PermissionManager} used by this instance.</p> */
    private PermissionManager permissionManager = null;
    /** <p>The {@link UserManager} used by this instance.</p> */
    private UserManager userManager = null;
    /** <p>The {@link ThumbnailManager} used by this instance.</p> */
    private ThumbnailManager thumbnailManager = null;

    /**
     * <p>Create a new {@link ExportEngine} instance.</p>
     */
    public ExportEngine() {
        ContainerManager.autowireComponent(this);
    }

    /* ====================================================================== */
    /* SPRING AUTO-WIRING SETTERS                                             */
    /* ====================================================================== */

    /**
     * <p>Setter for Spring's component wiring.</p>
     */
    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
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
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * <p>Setter for Spring's component wiring.</p>
     */
    public void setThumbnailManager(ThumbnailManager thumbnailManager) {
        this.thumbnailManager = thumbnailManager;
    }

    /* ====================================================================== */
    /* MAIN METHODS FOR CONTENT EXPORT                                        */
    /* ====================================================================== */

    /**
     * <p>Export all the content from all specified spaces.</p>
     */
    public void export(String spaceKeys[], Notifiable notifiable,
                       boolean exportPages) {
        for (int x = 0; x < spaceKeys.length; x ++) {
            this.export(spaceKeys[x], notifiable, exportPages);
        }
    }

    /**
     * <p>Export all the content from the specified space.</p>
     */
    public void export(String spaceKey, Notifiable notifiable,
                       boolean exportPages) {
        final Space space = this.spaceManager.getSpace(spaceKey);
        this.export(space, notifiable, exportPages);
    }

    /**
     * <p>Export all the content from the specified space.</p>
     */
    public void export(Space space, Notifiable notifiable,
                        boolean exportPages) {
        if (space == null) return;
        
        this.message(notifiable, "msg.exporting-space", space);

        final List locked = new ArrayList();
        final List list = this.spaceManager.getPages(space, true);
        final Permission permission = Permission.VIEW;
        final String userName = ConfigurationManager.INSTANCE.getUserName();
        User user = null;
        try {
            user = this.userManager.getUser(userName);
        } catch (EntityException exception) {
            this.error(notifiable, "err.user-lookup", new Object[] { userName },
                       exception);
        }

        for (final Iterator iterator = list.iterator(); iterator.hasNext(); ) {
            final Page page = (Page) iterator.next();
            if (this.permissionManager.hasPermission(user, permission, page)) {
                if (exportPages) this.export(page, notifiable);
            } else {
                locked.add(page);
            }
        }

        final String styleData = StylesheetAction.renderSpaceStylesheet(space);
        final File spaceDir = ExportUtilities.getDirectory(space);
        final File styleFile = new File(spaceDir, "space.css");
        try {
            if (! spaceDir.isDirectory()) spaceDir.mkdirs();
            FileOutputStream stream = new FileOutputStream(styleFile);
            OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
            writer.write(styleData);
            writer.flush();
            stream.flush();
            writer.close();
            stream.close();
        } catch (IOException exception) {
            this.error(notifiable, "err.stylesheet", new Object[] { styleFile },
                       exception);
        }

        for (final Iterator iterator = locked.iterator(); iterator.hasNext(); ) {
            final Page page = (Page) iterator.next();
            this.message(notifiable, "msg.locked-page", page);
        }

        this.message(notifiable, "msg.exported-space", space);
    }
    
    /**
     * <p>Export the specified page.</p>
     */
    public void export(Page page, Notifiable notifiable) {
        this.message(notifiable, "msg.exporting-page", page);
        
        final String userName = ConfigurationManager.INSTANCE.getUserName();
        User user = null;
        try {
            user = this.userManager.getUser(userName);
        } catch (EntityException exception) {
            this.error(notifiable, "err.user-lookup", new Object[] { userName },
                       exception);
        }

        try {
            final Template template = this.templatesManager.getTemplate(page.getSpaceKey());
            final ExportRenderer renderer = new ExportRenderer(page, user, notifiable);
            final VelocityContext context = new VelocityContext();
            context.put("generalUtil", new GeneralUtil());
            context.put("renderer", renderer);
            context.put("page", page);

            final File pageFile = ExportUtilities.getFile(page);
            final File spaceDir = pageFile.getParentFile();
            if (! spaceDir.isDirectory()) spaceDir.mkdirs();
            try {
                FileOutputStream stream = new FileOutputStream(pageFile);
                OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
                template.merge(context, writer);
                writer.flush();
                stream.flush();
                writer.close();
                stream.close();
            } catch (Exception exception) {
                this.error(notifiable, "err.exporting-page", page, exception);
            }
            
            final Iterator iterator = page.getAttachments().iterator();

            while (iterator.hasNext()) {
                final Attachment attachment = (Attachment) iterator.next();

                final File aFile = ExportUtilities.getAttachmentFile(attachment);

                final File aDir = aFile.getParentFile();
                if (! aDir.isDirectory()) aDir.mkdirs();

                try {
                    final InputStream aInput = attachment.getContentsAsStream();
                    FileUtils.copyFile(aInput, aFile, true);
                    aInput.close();
                    this.message(notifiable, "msg.exported-attachment",
                                 new Object[] { page.getTitle(),
                                                page.getSpaceKey(),
                                                aFile.getName() });
                } catch (IOException exception) {
                    this.error(notifiable, "err.exporting-attachment", page,
                               aFile.getName(), exception);
                }


                if (! this.thumbnailManager.isThumbnailable(attachment)) {
                    continue;
                }

                /* We need to verify if the thumbnail exists or not */
                InputStream tInput = null;
                try {
                    tInput = this.thumbnailManager.getThumbnailData(attachment);
                } catch (IOException exception) {
                    this.message(notifiable, "msg.no-thumbnail",
                                 new Object[] { page.getTitle(),
                                                page.getSpaceKey(),
                                                attachment.getFileName() });
                    continue;
                }

                final File tFile = ExportUtilities.getThumbnailFile(attachment);
                final File tDir = tFile.getParentFile();
                if (! tDir.isDirectory()) tDir.mkdirs();

                try {
                    FileUtils.copyFile(tInput, tFile, true);
                    tInput.close();
                    notifiable.notify("Exported thumbnail " + tFile.getName());
                } catch (IOException exception) {
                    this.error(notifiable, "err.exporting-thumbnail", page,
                               aFile.getName(), exception);
                }
            }

        } catch (Exception exception) {
            this.error(notifiable, "err.exporting-page", page, exception);
        }

        this.message(notifiable, "msg.exported-page", page);
    }

    /**
     * <p>Remove all resources associated with the specified page.</p>
     */
    public void remove(String spaceKey, String pageTitle, Notifiable notifiable) {
        File files[] = ExportUtilities.getFiles(spaceKey, pageTitle, null);
        for (int x = 0; x < files.length; x ++) {
            this.message(notifiable, "msg.removing-page",
                         new Object[] { pageTitle, spaceKey,
                                        files[x].getName() });
            FileUtils.recursiveDelete(files[x]);
        }
    }

    /* ====================================================================== */
    /* PRIVATE METHODS FOR ERROR AND MESSAGES NOTIFICATION                    */
    /* ====================================================================== */

    private void error(Notifiable notifiable, String key, Page page,
                       Throwable exception) {
        final Object params[] = page == null ? null :
            new Object[] { page.getTitle(),
                           page.getSpace().getName() };
        final String message = this.localizeMessage(key, params);
        if (notifiable != null) {
            notifiable.notify(message);
            notifiable.notify(exception);
        }
        this.log.warn(message, exception);
    }
    
    private void error(Notifiable notifiable, String key,
                       Object args[], Throwable exception) {
        final String message = this.localizeMessage(key, args);
        if (notifiable != null) {
            notifiable.notify(message);
            notifiable.notify(exception);
        }
        this.log.warn(message, exception);
    }

    private void error(Notifiable notifiable, String key, Page page,
                       Object arg, Throwable exception) {
        final Object params[] = page == null ? null :
            new Object[] { page.getTitle(),
                           page.getSpace().getName(),
                           arg };
        final String message = this.localizeMessage(key, params);
        if (notifiable != null) {
            notifiable.notify(message);
            notifiable.notify(exception);
        }
        this.log.warn(message, exception);
    }

    private void message(Notifiable notifiable, String key, Space space) {
        final Object params[] = space == null ? null :
                                new Object[] { space.getName() };
        final String message = this.localizeMessage(key, params);
        if (notifiable != null) notifiable.notify(message);
        this.log.info(message);
    }

    private void message(Notifiable notifiable, String key, Page page) {
        final Object params[] = page == null ? null :
                                new Object[] { page.getTitle(),
                                               page.getSpace().getName() };
        final String message = this.localizeMessage(key, params);
        if (notifiable != null) notifiable.notify(message);
        this.log.info(message);
    }
    
    private void message(Notifiable notifiable, String key, Object args[]) {
        final String message = this.localizeMessage(key, args);
        if (notifiable != null) notifiable.notify(message);
        this.log.info(message);
    }
    
}
