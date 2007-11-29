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

import it.could.confluence.autoexport.SingletonManager;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.BlogPost;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.setup.BootstrapManager;
import com.atlassian.confluence.setup.BootstrapUtils;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.util.GeneralUtil;
import com.opensymphony.util.TextUtils;

/**
 * <p>An extremely simple utilty class that can be used by templates for
 * dumping different strings.</p>
 */
public final class ExportUtils {

    /** <p>The {@link BootstrapManager} to get the Confluence base URL.</p> */
    private static final BootstrapManager bootstrapManager =
                                           BootstrapUtils.getBootstrapManager(); 

    /** <p>Constructor to give instances to Velocity.</p> */
    public ExportUtils() {
        super();
    }

    /**
     * <p>Return a simple link to the specified {@link AbstractPage}.</p>
     */
    public static String link(AbstractPage page) {
        final String title = TextUtils.htmlEncode(page.getTitle()); 
        return new StringBuffer("<a href=\"")
            .append(bootstrapManager.getBaseUrl())
            .append(page.getUrlPath())
            .append("\" title=\"")
            .append(title)
            .append("\">")
            .append(title)
            .append("</a>")
            .toString();
    }

    /**
     * <p>Return a simple link for the specified {@link Space}.</p>
     */
    public static String link(Space space) {
        final Page home = space.getHomePage();
        final String name = TextUtils.htmlEncode(space.getName()); 
        return new StringBuffer("<a href=\"")
            .append(bootstrapManager.getBaseUrl())
            .append(home.getUrlPath())
            .append("\" title=\"")
            .append(name)
            .append("\">")
            .append(name)
            .append("</a>")
            .toString();
    }

    /**
     * <p>Return all the breadcrumbs for the specified {@link Page}.</p>
     * 
     * <p>The default separator used is "<code>&gt;</code>" (greater than).</p>
     * 
     * @see #breadcrumbs(Page, String)
     */
    public static String breadcrumbs(Page page) {
        return breadcrumbs(page, ">");
    }

    /**
     * <p>Return all the breadcrumbs for the specified {@link Page} separated
     * by the specified {@link String}.</p>
     */
    public static String breadcrumbs(Page page, String separator) {
        final String s = "&nbsp;" + TextUtils.htmlEncode(separator) + "&nbsp;";

        final StringBuffer buffer = new StringBuffer();
        final Page parent = page.getParent();
        if (parent != null) buffer.append(breadcrumbs(parent, separator));
        else buffer.append(link(page.getSpace()));

        return buffer.append(s).append(link(page)).toString();
    }

    /**
     * <p>Return all the breadcrumbs for the specified {@link BlogPost}.</p>
     * 
     * <p>The default separator used is "<code>&gt;</code>" (greater than).</p>
     * 
     * @see #breadcrumbs(BlogPost, String)
     */
    public static String breadcrumbs(BlogPost post) {
        return breadcrumbs(post, ">");
    }

    /**
     * <p>Return all the breadcrumbs for the specified {@link BlogPost}
     * separated by the specified {@link String}.</p>
     */
    public static String breadcrumbs(BlogPost post, String separator) {
        final String s = "&nbsp;" + TextUtils.htmlEncode(separator) + "&nbsp;";

        final Date date = post.getCreationDate();
        return new StringBuffer(link(post.getSpace()))
            .append(s).append(new SimpleDateFormat("yyyy").format(date))
            .append(s).append(new SimpleDateFormat("MM").format(date))
            .append(s).append(new SimpleDateFormat("dd").format(date))
            .append(s).append(link(post))
            .toString();
    }

    /**
     * <p>Return a link to Atlassian Confluence's home page and its
     * version details.</p>
     */
    public static String getConfluenceInfo() {
        return new StringBuffer()
            .append("<a href=\"http://www.atlassian.com/confluence/\">")
            .append("Atlassian Confluence</a> (Version: ")
            .append(GeneralUtil.getVersionNumber())
            .append(" Build: ")
            .append(GeneralUtil.getBuildNumber())
            .append(" ")
            .append(GeneralUtil.getBuildDateString())
            .append(")").toString();
    }
    
    /**
     * <p>Return a link to the AutoExport plugin's home page and its
     * version details.</p>
     */
    public static String getAutoexportInfo() {
        return new StringBuffer("<a href=\"")
            .append(SingletonManager.PLUGIN_URL)
            .append("\">")
            .append(SingletonManager.PLUGIN_NAME)
            .append("</a> (Version: ")
            .append(SingletonManager.PLUGIN_VERSION)
            .append(")").toString();
    }
}
