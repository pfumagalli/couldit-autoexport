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

import it.could.confluence.autoexport.ConfigurationManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.core.SpaceContentEntityObject;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.BlogPost;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.spaces.Space;

public class ExportUtilities {
    
    private static final String ATTACHMENTS_DIR_EXT = ".data";
    private static final String THUMBNAILS_FILE_EXT = ".jpeg";

    /**
     * <p>Deny public construction of this class.</p>
     */
    private ExportUtilities() {
        super();
    }

    private static File getBaseFile(String space, String title,
                                    String extension, Date date) {
        File root = new File(ConfigurationManager.INSTANCE.getRootPath());
        root = new File(root, mangle(space));

        if (date != null) {
            root = new File(root, new SimpleDateFormat("yyyy").format(date));
            root = new File(root, new SimpleDateFormat("MM").format(date));
            root = new File(root, new SimpleDateFormat("dd").format(date));
        }

        return new File(root, mangle(title) + extension);
    }

    private static File getBaseFile(String space, String title, Date date,
                                    String fileName, String extension) {
        final File base = getBaseFile(space, title, ATTACHMENTS_DIR_EXT, date);
        if (extension == null) return new File(base, fileName);
        else return new File(base, fileName + extension);
    }

    /* ====================================================================== */
    /* METHODS RETURNING THE DIFFERENT DIRECTORIES USED FOR EXPORT            */
    /* ====================================================================== */
    
    public static File getDirectory(Space space) {
        final String rootPath = ConfigurationManager.INSTANCE.getRootPath();
        final String mangledSpace = mangle(space.getKey());
        final File rootDirectory = new File(rootPath);
        return new File(rootDirectory, mangledSpace);
    }

    public static File getAttachmentFile(Attachment attachment) {
        ContentEntityObject entity = attachment.getContent();
        if (entity instanceof Page) {
            final Page page = (Page) entity;
            return getBaseFile(page.getSpaceKey(),
                               page.getTitle(),
                               null,
                               attachment.getFileName(),
                               null);

        } else if (entity instanceof BlogPost) {
            final BlogPost blog = (BlogPost) entity;
            return getBaseFile(blog.getSpaceKey(),
                               blog.getTitle(),
                               blog.getCreationDate(),
                               attachment.getFileName(),
                               null);
        }
        throw new IllegalArgumentException("Wrong attachment owner type: "
                                           + entity.getClass().getName());
    }

    public static File getThumbnailFile(Attachment attachment) {
        ContentEntityObject entity = attachment.getContent();
        if (entity instanceof Page) {
            final Page page = (Page) entity;
            return getBaseFile(page.getSpaceKey(),
                               page.getTitle(),
                               null,
                               attachment.getFileName(),
                               THUMBNAILS_FILE_EXT);

        } else if (entity instanceof BlogPost) {
            final BlogPost blog = (BlogPost) entity;
            return getBaseFile(blog.getSpaceKey(),
                               blog.getTitle(),
                               blog.getCreationDate(),
                               attachment.getFileName(),
                               THUMBNAILS_FILE_EXT);
        }
        throw new IllegalArgumentException("Wrong attachment owner type: "
                                           + entity.getClass().getName());
    }

    public static File getFile(Page page) {
        return getBaseFile(page.getSpaceKey(),
                           page.getTitle(),
                           ConfigurationManager.INSTANCE.getExtension(),
                           null);
    }

    public static File getFile(BlogPost blog) {
        return getBaseFile(blog.getSpaceKey(),
                           blog.getTitle(),
                           ConfigurationManager.INSTANCE.getExtension(),
                           blog.getCreationDate());
    }
    
    public static File[] getFiles(String spaceKey, String title, Date date) {
        File files[] = new File[2];
        files[0] = getBaseFile(spaceKey, title,
                               ConfigurationManager.INSTANCE.getExtension(),
                               date);
        files[1] = getBaseFile(spaceKey, title, ATTACHMENTS_DIR_EXT, date);
        return files;
    }

    /* ====================================================================== */
    /* METHODS COMPUTING THE HREFs LINKING DIFFERENT PIECES OF CONTENT        */
    /* ====================================================================== */

    private static StringBuffer getBaseLink(SpaceContentEntityObject from,
                                            String space, String title,
                                            String extension, Date date) {
        final StringBuffer buffer = new StringBuffer();
        
        if (from instanceof BlogPost) buffer.append("../../../");

        if (! from.getSpaceKey().equals(space)) {
            buffer.append("../").append(mangle(space)).append('/');
        }

        if (date != null) { 
            buffer.append(new SimpleDateFormat("yyyy/MM/dd/").format(date));
        }
        
        return buffer.append(mangle(title)).append(extension);
    }

    private static StringBuffer getBaseLink(SpaceContentEntityObject from,
                                            String space, String title,
                                            Date date, String fileName,
                                            String extension) {
        final StringBuffer buffer = getBaseLink(from, space, title,
                                                ATTACHMENTS_DIR_EXT, date);
        buffer.append('/');
        buffer.append(fileName);
        if (extension != null) buffer.append(extension);
        return buffer;
    }

    public static String getLink(SpaceContentEntityObject from, BlogPost toBlog) {
        return getBaseLink(from, toBlog.getSpaceKey(), toBlog.getTitle(),
                           ConfigurationManager.INSTANCE.getExtension(),
                           toBlog.getCreationDate()).toString();
    }

    public static String getLink(SpaceContentEntityObject from, Page toPage) {
        return getBaseLink(from, toPage.getSpaceKey(), toPage.getTitle(),
                           ConfigurationManager.INSTANCE.getExtension(),
                           null).toString();
    }

    public static String getLink(SpaceContentEntityObject from, Space toSpace) {
        return getLink(from, toSpace.getHomePage());
    }

    public static String getAttachmentLink(SpaceContentEntityObject from,
                                           Attachment attachment) {
        ContentEntityObject to = attachment.getContent();
        if (to instanceof Page) {
            Page page = (Page) to;
            return getBaseLink(from,
                               page.getSpaceKey(),
                               page.getTitle(),
                               null,
                               attachment.getFileName(),
                               null).toString();
        } else if (to instanceof BlogPost) {
            BlogPost blog = (BlogPost) to;
            return getBaseLink(from,
                               blog.getSpaceKey(),
                               blog.getTitle(),
                               blog.getCreationDate(),
                               attachment.getFileName(),
                               null).toString();
        }

        throw new IllegalArgumentException("Wrong attachment owner type: "
                                           + to.getClass().getName());
    }

    public static String getThumbnailLink(SpaceContentEntityObject from,
                                          Attachment attachment) {
        ContentEntityObject to = attachment.getContent();
        if (to instanceof Page) {
            Page page = (Page) to;
            return getBaseLink(from,
                               page.getSpaceKey(),
                               page.getTitle(),
                               null,
                               attachment.getFileName(),
                               THUMBNAILS_FILE_EXT).toString();
            } else if (to instanceof BlogPost) {
                BlogPost blog = (BlogPost) to;
                return getBaseLink(from,
                                   blog.getSpaceKey(),
                                   blog.getTitle(),
                                   blog.getCreationDate(),
                                   attachment.getFileName(),
                                   THUMBNAILS_FILE_EXT).toString();
            }

        throw new IllegalArgumentException("Wrong attachment owner type: "
                                           + to.getClass().getName());
    }

    /* ====================================================================== */
    /* FILENAME MANGLING                                                      */
    /* ====================================================================== */

    private static String mangle(String string) {
        StringBuffer buffer = new StringBuffer();
        char array[] = string.toCharArray();
        boolean separated = true;
        for (int x = 0; x < array.length; x++) {
            if (Character.isLetterOrDigit(array[x])) {
                buffer.append(Character.toLowerCase(array[x]));
                separated = false;
            } else if (Character.isWhitespace(array[x])) {
                if (separated) continue;
                buffer.append('-');
                separated = true;
            } else {
                buffer.append('X');
            }
        }
        return buffer.toString();
    }
    
}
