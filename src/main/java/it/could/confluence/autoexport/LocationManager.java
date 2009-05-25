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

import it.could.confluence.localization.LocalizedComponent;
import it.could.util.location.Location;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.BlogPost;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.user.EntityException;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;

public class LocationManager extends LocalizedComponent {
    
    private static final String SPACE_RESOURCES_DIR = "resources/";
    private static final String ATTACHMENTS_DIR_EXT = ".data/";
    private static final String THUMBNAILS_FILE_EXT = ".jpeg";
    private static final Location AUTOEXPORT_LOCATION = Location.parse("autoexport:///.");

    /** <p>The {@link ConfigurationManager} used by this instance.</p> */
    private final ConfigurationManager configurationManager;
    /** <p>The {@link PermissionManager} used by this instance.</p> */
    private final PermissionManager permissionManager;
    /** <p>The {@link UserManager} used by this instance.</p> */
    private final UserManager userManager;

    /** <p>Create a new {@link LocationManager} instance.</p> */
    public LocationManager(ConfigurationManager configurationManager,
                    PermissionManager permissionManager,
                    UserManager userManager) {

        this.configurationManager = configurationManager;
        this.permissionManager = permissionManager;
        this.userManager = userManager;

        this.log.info("Instance created");
    }

    /* ====================================================================== */
    /* PRIVATE METHODS                                                        */
    /* ====================================================================== */

    private String getMangledTitle(AbstractPage page) {
        StringBuffer buffer = new StringBuffer();
        char array[] = page.getTitle().toLowerCase().toCharArray();
        boolean separated = true;
        for (int x = 0; x < array.length; x++) {
            if ("abcdefghijklmnopqrstuvwxyz0123456789".indexOf(array[x]) >= 0) {
                buffer.append(Character.toLowerCase(array[x]));
                separated = false;
            } else if ("\r\n\t -".indexOf(array[x]) >= 0) {
                if (separated) continue;
                buffer.append('-');
                separated = true;
            }
        }
        if (buffer.length() == 0) return Long.toString(page.getId());
        return buffer.toString();
    }

    private Location getRelativeLocation(Space space, String extension) {
        final StringBuffer buffer = new StringBuffer(space.getKey());
        buffer.append('/');
        if (extension != null) buffer.append(extension);
        return Location.parse(buffer.toString());
    }

    private Location getRelativeLocation(AbstractPage page, String extension) {
        StringBuffer buffer = new StringBuffer();
        if (page instanceof BlogPost) {
            final Date date = page.getCreationDate();
            buffer.append(new SimpleDateFormat("yyyy/MM/dd/").format(date));
        }

        buffer.append(this.getMangledTitle(page));
        if (extension != null) buffer.append(extension);
        
        return this.getRelativeLocation(page.getSpace(), buffer.toString());
    }

    /* ====================================================================== */
    /* PERMISSION METHODS                                                     */
    /* ====================================================================== */

    /**
     * <p>Check if the specified {@link Object} can be exported by the
     * {@link ConfigurationManager#getUserName() autoexport user}.</p>
     */
    public boolean exportable(Object object) {
        final String name = this.configurationManager.getUserName();
        try {
            final User user = this.userManager.getUser(name);
            final Permission perm = Permission.VIEW;
            return this.permissionManager.hasPermission(user, perm, object);
        } catch (EntityException exception) {
            final Object args[] = new Object[] { name };
            final String msg = this.localizeMessage("err.user-lookup", args);
            this.log.warn(msg, exception);
            return false;
        }
    }

    /* ====================================================================== */
    /* FILE METHODS                                                           */
    /* ====================================================================== */

    public File getFile(AbstractPage page) {
        final String extension = this.configurationManager.getExtension();
        final String rootPath = this.configurationManager.getRootPath();

        final Location relative = this.getRelativeLocation(page, extension);
        final Location root = Location.parse("file://" + rootPath + "/");
        final Location resolved = root.resolve(relative);
        return resolved.getPath().toFile();
    }

    public File getFile(Space space, String resource) {
        final String extension = SPACE_RESOURCES_DIR + resource;
        final String rootPath = this.configurationManager.getRootPath();

        final Location relative = this.getRelativeLocation(space, extension);
        final Location root = Location.parse("file://" + rootPath + "/");
        final Location resolved = root.resolve(relative);
        return resolved.getPath().toFile();
    }

    public File getFile(Attachment attachment, boolean thumbnail) {
        final String rootPath = this.configurationManager.getRootPath();
        final StringBuffer buffer = new StringBuffer(ATTACHMENTS_DIR_EXT);
        buffer.append(attachment.getFileName());
        if (thumbnail) buffer.append(THUMBNAILS_FILE_EXT);
        final String extension = buffer.toString();
        final AbstractPage page = (AbstractPage) attachment.getContent();

        final Location relative = this.getRelativeLocation(page, extension);
        final Location root = Location.parse("file://" + rootPath + "/");
        final Location resolved = root.resolve(relative);
        return resolved.getPath().toFile();
    }

    /* ====================================================================== */
    /* LOCATION METHODS                                                       */
    /* ====================================================================== */
    
    public Location getLocation(AbstractPage page) {
        if (! this.exportable(page)) {
            final String base = this.configurationManager.getConfluenceUrl(); 
            return Location.parse(base + page.getUrlPath());
        }

        final String extension = this.configurationManager.getExtension();

        final Location relative = this.getRelativeLocation(page, extension);
        return AUTOEXPORT_LOCATION.resolve(relative);
    }

    public Location getLocation(Space space, String resource) {
        /* Resources are only valid when exporting, don't check permissions */
        final String extension = SPACE_RESOURCES_DIR + resource;
        final Location relative = this.getRelativeLocation(space, extension);
        return AUTOEXPORT_LOCATION.resolve(relative);
    }

    public Location getLocation(Attachment attachment, boolean thumbnail) {
        if (! this.exportable(attachment)) {
            final String base = this.configurationManager.getConfluenceUrl(); 
            return Location.parse(base + attachment.getUrlPath());
        }

        final StringBuffer buffer = new StringBuffer(ATTACHMENTS_DIR_EXT);
        buffer.append(attachment.getFileName());
        if (thumbnail) buffer.append(THUMBNAILS_FILE_EXT);
        final String extension = buffer.toString();
        final AbstractPage page = (AbstractPage) attachment.getContent();

        final Location relative = this.getRelativeLocation(page, extension);
        return AUTOEXPORT_LOCATION.resolve(relative);
    }
}
