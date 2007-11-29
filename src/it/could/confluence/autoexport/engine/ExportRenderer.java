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

import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

import bucket.container.ContainerManager;

import com.atlassian.confluence.core.SpaceContentEntityObject;
import com.atlassian.confluence.links.linktypes.AttachmentLink;
import com.atlassian.confluence.links.linktypes.BlogPostLink;
import com.atlassian.confluence.links.linktypes.PageLink;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.BlogPost;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.WikiStyleRenderer;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.setup.BootstrapManager;
import com.atlassian.confluence.setup.BootstrapUtils;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.embedded.EmbeddedImage;
import com.atlassian.renderer.embedded.EmbeddedResource;
import com.atlassian.renderer.embedded.EmbeddedResourceRenderer;
import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.links.LinkRenderer;
import com.atlassian.renderer.links.UrlLink;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;

/**
 * <p>A bean rendering one single {@link Page} or {@link BlogPost} and all its
 * links and embedded resources.</p>
 */
public class ExportRenderer extends ComponentSupport
implements LinkRenderer, EmbeddedResourceRenderer {

    /** <p>The {@link RenderMode} for the body of links.</p> */
    private static final RenderMode LINKS_MODE =
            RenderMode.INLINE.and(RenderMode.suppress(RenderMode.F_LINKS));

    /** <p>The {@link WikiStyleRenderer} rendering content.</p> */
    private WikiStyleRenderer wikiStyleRenderer = null;
    /** <p>The {@link PageManager} used to look up pages.</p> */
    private PageManager pageManager = null;
    /** <p>The {@link UserManager} used to look up users.</p> */
    private UserManager userManager = null;
    /** <p>The {@link PermissionManager} used to verify permissions.</p> */
    private PermissionManager permissionManager = null;

    /** <p>The entity object that this instance will export.</p> */
    private final SpaceContentEntityObject entity;
    /** <p>The URI of the root Confluence installation.</p> */
    private final String confluenceUri;
    /** <p>The {@link User} used to export the content.</p> */
    private final User user;
    /** <p>The {@link Notfiable} instance to report errors to.</p> */
    private final Notifiable notifiable;

    /**
     * <p>Create a new {@link ExportRenderer} instance.</p>
     * 
     * @param page the {@link Page} exported by {@link #render()}.
     * @param user the {@link User} used to export the content.
     */
    public ExportRenderer(Page page, User user, Notifiable notifiable) {
        this((SpaceContentEntityObject) page, user, notifiable);
    }

    /**
     * <p>Create a new {@link ExportRenderer} instance.</p>
     * 
     * @param page the {@link BlogPost} exported by {@link #render()}.
     * @param user the {@link User} used to export the content.
     */
    public ExportRenderer(BlogPost blog, User user, Notifiable notifiable) {
        this((SpaceContentEntityObject) blog, user, notifiable);
    }

    /* ====================================================================== */
    /* INTERNAL METHODS PRIVATE TO THIS INSTANCE                              */
    /* ====================================================================== */

    /**
     * <p>Create a new {@link ExportRenderer} instance.</p>
     */
    private ExportRenderer(SpaceContentEntityObject entity, User user,
                           Notifiable notifiable) {
        ContainerManager.autowireComponent(this);
        
        final BootstrapManager bsm = BootstrapUtils.getBootstrapManager();
        final StringBuffer buf = new StringBuffer();
        buf.append(bsm.getProperty(BootstrapManager.CONFLUENCE_DOMAIN_PROP));
        buf.append(bsm.getProperty(BootstrapManager.WEBAPP_CONTEXT_PATH_KEY));
        this.confluenceUri = buf.toString();
        this.notifiable = notifiable;
        this.entity = entity;
        this.user = user;
    }

    /**
     * <p>Check if the current {@link User} has access to the rights to view
     * the specified object (a {@link Page}, {@link BlogPost} or {@link Space}
     * for example).</p>
     */
    private boolean canView(Object object) {
        final Permission perm = Permission.VIEW;
        return this.permissionManager.hasPermission(this.user, perm, entity);
    }

    /**
     * <p>Render the specified message as an error.</p>
     */
    private String error(RenderContext context, String key) {
        final String message = this.localizeMessage(key);
        this.notifiable.notify(message);
        this.log.warn(message);
        StringBuffer buffer = new StringBuffer("<span class=\"error\">");
        buffer.append(message);
        buffer.append("</span>");
        return context == null ?
               buffer.toString() :
               context.addRenderedContent(buffer.toString());
    }

    /**
     * <p>Render the specified message as an error.</p>
     */
    private String error(RenderContext context, String key, Object argument) {
        return this.error(context, key, new Object[] { argument });
    }

    /**
     * <p>Render the specified message as an error.</p>
     */
    private String error(RenderContext context, String key, Object arguments[]) {
        final String message = this.localizeMessage(key, arguments);
        this.notifiable.notify(message);
        this.log.warn(message);

        final StringBuffer buffer = new StringBuffer("<span class=\"error\">");
        buffer.append(message);
        buffer.append("</span>");
        return context == null ?
               buffer.toString() :
               context.addRenderedContent(buffer.toString());
    }

    /**
     * <p>Render the specified message as an error.</p>
     */
    private String error(RenderContext context, String key, Object arguments[],
                         Throwable throwable) {
        final String message = this.localizeMessage(key, arguments);
        this.notifiable.notify(message);
        this.notifiable.notify(throwable);
        this.log.warn(message, throwable);

        final StringBuffer buffer = new StringBuffer("<span class=\"error\">");
        buffer.append(message);
        buffer.append("</span>");
        return context == null ?
               buffer.toString() :
               context.addRenderedContent(buffer.toString());
    }

    /* ====================================================================== */
    /* SPRING AUTO-WIRING SETTERS                                             */
    /* ====================================================================== */

    /**
     * <p>Setter for Spring's component wiring.</p>
     */
    public void setWikiStyleRenderer(WikiStyleRenderer wikiStyleRenderer) {
        this.wikiStyleRenderer = wikiStyleRenderer;
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
    public void setPageManager(PageManager pageManager) {
        this.pageManager = pageManager;
    }

    /**
     * <p>Setter for Spring's component wiring.</p>
     */
    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    /* ====================================================================== */
    /* EXPOSED RENDERING METHODS                                              */
    /* ====================================================================== */

    /**
     * <p>Render the content from the entity specified at construction.</p>
     */
    public String getContent() {
        final PageContext context = this.entity.toPageContext();

        context.setAttachmentsPath("http://theAttachmentsPath/");
        context.setBaseUrl("http://theBaseUrl/");
        context.setCharacterEncoding("UTF-8");
        context.setEmbeddedResourceRenderer(this);
        context.setImagePath("http://theImagePath/");
        context.setLinkRenderer(this);
        context.setSiteRoot("http://theSiteRoot/");
        context.setOutputType(PageContext.DISPLAY);
        context.setRenderingForWysiwyg(false);

        try {
            final String content = this.entity.getContent();
            return this.wikiStyleRenderer.convertWikiToXHtml(context, content);
        } catch (Throwable throwable) {
            return this.error(null, "err.rendering", new Object[] { 
                                                       this.entity.getTitle(),
                                                       this.entity.getSpaceKey()
                                                     }, throwable);                                                        
        }
    }

    /**
     * <p>Return the creation details of the entity being rendered.</p>
     */
    public String getCreationDetails() {
        try {
            final String userName = this.entity.getCreatorName();
            final User user = this.userManager.getUser(userName);
            final Object[] arguments = {
                    this.confluenceUri + "/display/~" + user.getName(),
                    user.getFullName(),
                    this.entity.getCreationDate()};
            return this.localizeMessage("msg.creator", arguments);
        } catch (Exception exception) {
            final Object[] arguments = {
                    this.entity.getCreatorName(),
                    this.entity.getCreationDate()};
            return this.localizeMessage("msg.creatorNoLink", arguments);
        }
    }

    /**
     * <p>Return the last modification details of the entity being rendered.</p>
     */
    public String getModificationDetails() {
        try {
            final String userName = this.entity.getLastModifierName();
            final User user = this.userManager.getUser(userName);
            final Object[] arguments = {
                    this.confluenceUri + "/display/~" + user.getName(),
                    user.getFullName(),
                    this.entity.getLastModificationDate()};
            return this.localizeMessage("msg.modifier", arguments);
        } catch (Exception exception) {
            final Object[] arguments = {
                    this.entity.getLastModifierName(),
                    this.entity.getLastModificationDate()};
            return this.localizeMessage("msg.modifierNoLink", arguments);
        }
    }

    /**
     * <p>Return the URI pointing to the current Confluence root.</p>
     */
    public String getConfluenceUri() {
        return this.confluenceUri;
    }

    /**
     * <p>Prepare a link from the current page or blog post to the specified
     * {@link Page}.</p>
     */
    public String linkTo(Page page) {
        if (page == null) return null;
        StringBuffer buffer = new StringBuffer("<a href=\"");
        if (this.canView(page)) {
            buffer.append(ExportUtilities.getLink(this.entity, page));
        } else {
            buffer.append(this.confluenceUri);
            buffer.append(page.getUrlPath());
        }
        buffer.append("\" title=\"");
        buffer.append(page.getTitle());
        buffer.append("\">");
        buffer.append(page.getTitle());
        buffer.append("</a>");
        return buffer.toString();
    }

    /**
     * <p>Prepare a link from the current page or blog post to the specified
     * {@link BlogPost}.</p>
     */
    public String linkTo(BlogPost blog) {
        if (blog == null) return null;
        StringBuffer buffer = new StringBuffer("<a href=\"");
        if (this.canView(blog)) {
            buffer.append(ExportUtilities.getLink(this.entity, blog));
        } else {
            buffer.append(this.confluenceUri);
            buffer.append(blog.getUrlPath());
        }
        buffer.append("\" title=\"");
        buffer.append(blog.getTitle());
        buffer.append("\">");
        buffer.append(blog.getTitle());
        buffer.append("</a>");
        return buffer.toString();
    }

    /**
     * <p>Prepare a link from the current page or blog post to the specified
     * {@link Space}.</p>
     */
    public String linkTo(Space space) {
        if (space == null) return null;
        StringBuffer buffer = new StringBuffer("<a href=\"");
        if (this.canView(space)) {
            buffer.append(ExportUtilities.getLink(this.entity, space));
        } else {
            buffer.append(this.confluenceUri);
            buffer.append(space.getHomePage().getUrlPath());
        }
        buffer.append("\" title=\"");
        buffer.append(space.getName());
        buffer.append("\">");
        buffer.append(space.getName());
        buffer.append("</a>");
        return buffer.toString();
    }

    /* ====================================================================== */
    /* METHODS REQUIRED BY THE INTERFACES WE IMPLEMENT                        */
    /* ====================================================================== */

    /**
     * <p>Render the specified {@link Link}.</p>
     */
    public String renderLink(Link link, RenderContext context) {
        final StringBuffer buffer = new StringBuffer("<a href=\"");

        if (link instanceof PageLink) {
            final PageLink pageLink = (PageLink) link;
            final String spaceKey = pageLink.getSpaceKey();
            final String pageTitle = pageLink.getTitle();
            final Page page = this.pageManager.getPage(spaceKey, pageTitle);
            
            if (this.canView(page)) {
                buffer.append(ExportUtilities.getLink(this.entity, page));
            } else {
                buffer.append(this.confluenceUri);
                buffer.append(pageLink.getUrl());
            }

        } else if (link instanceof BlogPostLink) {
            final BlogPostLink blogLink = (BlogPostLink) link;
            final String spaceKey = blogLink.getSpaceKey();
            final String blogTitle = blogLink.getTitle();
            final Calendar calendar = blogLink.getPostingDay();
            final BlogPost blog = this.pageManager.getBlogPost(spaceKey,
                                                           blogTitle, calendar);

            if (this.canView(blog)) {
                buffer.append(ExportUtilities.getLink(this.entity, blog));
            } else {
                buffer.append(this.confluenceUri);
                buffer.append(blogLink.getUrl());
            }

        } else if (link instanceof AttachmentLink) {
            final AttachmentLink attachmentLink = (AttachmentLink) link;
            final Attachment attachment = attachmentLink.getAttachment();

            if (this.canView(attachment.getContent())) {
                buffer.append(ExportUtilities.getAttachmentLink(this.entity,
                                                                attachment));
            } else {
                buffer.append(this.confluenceUri);
                buffer.append(attachmentLink.getUrl());
            }

        } else if (link instanceof UrlLink) {
            buffer.append(link.getUrl());

        } else {
            buffer.append(this.confluenceUri);
            buffer.append(link.getUrl());
        }

        final String title = link.getTitle();
        if ((title != null) && (title.length() > 0)) {
            buffer.append("\" title=\"");
            buffer.append(title);
        }
        buffer.append("\">");

        context.pushRenderMode(LINKS_MODE);
        final String body = link.getLinkBody();
        buffer.append(this.wikiStyleRenderer.convertWikiToXHtml(context, body));
        context.popRenderMode();
        
        buffer.append("</a>");

        return context.addRenderedContent(buffer.toString());
    }

    /**
     * <p>Render the specified {@link EmbeddedResource}.</p>
     */
    public String renderResource(EmbeddedResource resource, RenderContext context) {
        if (!(resource instanceof EmbeddedImage)) {
            return this.error(context, "err.resourcetype", resource.getClass().getName());
        }

        EmbeddedImage image = (EmbeddedImage) resource;

        final StringBuffer buffer = new StringBuffer("<img ");
        final Iterator iterator = image.getProperties().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry property = (Map.Entry) iterator.next();
            buffer.append(property.getKey());
            buffer.append("=\"");
            buffer.append(property.getValue());
            buffer.append("\" ");
        }
        buffer.append("src=\"");

        /* External image rendering */
        if (image.isExternal()) {
            if (image.isThumbNail()) {
                return this.error(context, "err.externalthumbnail", image.getUrl());
            } else {
                buffer.append(image.getUrl());
            }
            
        /* Internal image rendering */
        } else if (image.isInternal()) {
            final String fileName = image.getFilename();

            /* Values of "null" mean the current space/page, apparently */
            String toSpace = resource.getSpace();
            String toPage = resource.getPage();
            if (toSpace == null) toSpace = this.entity.getSpaceKey();
            if (toPage == null) toPage = this.entity.getTitle();

            /* If we can't resolve the page, then we fail */
            final Page page = this.pageManager.getPage(toSpace, toPage);
            if (page == null) return this.error(context, "err.embeddednopage");
            
            /* Check permissions before proceeding! */
            if (this.canView(page)) {
                /* Verify that this is an attachment */
                final Attachment attachment = page.getAttachmentNamed(fileName);
                if (attachment == null) return this.error(context, "err.embeddedattachment",
                            new Object[] { fileName, page.getTitle(), page.getSpaceKey() });

                /* Link either to the thumbnail or to the attachment */
                if (image.isThumbNail()) {
                    buffer.append(ExportUtilities.getThumbnailLink(this.entity,
                                                                   attachment));
                } else {
                    buffer.append(ExportUtilities.getAttachmentLink(this.entity,
                                                                    attachment));
                }
            } else {
                return this.error(context, "err.embeddedunaccessible",
                                  new Object[] { fileName, page.getTitle(),
                                                 page.getSpaceKey() });
            }

        }

        buffer.append("\">");
        return context.addRenderedContent(buffer.toString());
    }
}
