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
package it.could.confluence.localization;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import bucket.container.ContainerManager;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.opensymphony.xwork.ActionContext;

/**
 * <p>The {@link LocalizedAction} abstract class extendes Confluence abstract
 * basic action to fix some issues in plugins loading and provide extended
 * localization functionality.</p>
 */
public abstract class LocalizedAction extends ConfluenceActionSupport
implements LocalizedResource {

    /** <p>The {@link LocalizationHelper} used by this instance.</p> */
    private final LocalizationHelper bundle;

    /**
     * <p>Create a new instance of this {@link LocalizedAction}.</p>
     * 
     * <p>This method will automatically invoke Spring's
     * {@link ContainerManager#autowireComponent(Object) autowiring mechanism}
     * on this instance.</p> 
     */
    protected LocalizedAction() {
        final Locale locale = ActionContext.getContext().getLocale();
        this.bundle = LocalizationHelper.getBundle(this.getClass(), locale);
    }

    /**
     * <p>Return the localized version of the text identified by the specified
     * key.</p>
     */
    public String getText(String key) {
        try {
            return this.bundle.getString(key);
        } catch (Exception exception) {
            return super.getText(key);
        }
    }

    /**
     * <p>Return the localized version of the text identified by the specified
     * key or the default value if the key was not found.</p>
     */
    public String getText(String key, String defaultValue) {
        try {
            return this.bundle.getString(key);
        } catch (Exception exception) {
            return super.getText(key, defaultValue);
        }
    }

    /**
     * <p>Format the message identified by the specified format key according
     * to the specified array of arguments.</p>
     */
    public String getText(String formatKey, Object args[]) {
        try {
            return this.bundle.formatStringOrFail(formatKey, args);
        } catch (Exception exception) {
            return super.getText(formatKey, args);
        }
    }

    /**
     * <p>Format the message identified by the specified format key according
     * to the specified {@link List} of arguments.</p>
     */
    public String getText(String formatKey, List args) {
        try {
            return this.bundle.formatStringOrFail(formatKey, args.toArray());
        } catch (Exception exception) {
            return super.getText(formatKey, args);
        }
    }

    /**
     * <p>Format the message identified by the specified format key according
     * to the specified array of arguments.</p>
     * 
     * <p>If the format key was not found, then the default format specified
     * will be used to format the returned message.</p>
     */
    public String getText(String formatKey, String defaultFormat, List args)  {
        try {
            return this.bundle.formatStringOrFail(formatKey, args.toArray());
        } catch (Exception exception) {
            return super.getText(formatKey, defaultFormat, args);
        }
    }

    /**
     * <p>Return the {@link ResourceBundle} of this instance.</p>
     */
    public ResourceBundle getTexts() {
        return this.bundle;
    }

    /**
     * <p>Return the {@link ResourceBundle} associated with the
     * specified bundle name.</p>
     */
    public ResourceBundle getTexts(String name) {
        final ClassLoader loader = this.getClass().getClassLoader();
        try {
            return ResourceBundle.getBundle(name, this.getLocale(), loader);
        } catch (Throwable throwable) {
            return super.getTexts(name);
        }
    }

    /**
     * <p>Return the localized version of the message identified by the
     * specified key or the default value if the key was not found.</p>
     */
    public String localizeMessage(String key) {
        return this.getText(key, key);
    }

    /**
     * <p>Format the message identified by the specified format key according
     * to the specified array of arguments.</p>
     * 
     * <p>If the format key was not found, then the default format specified
     * will be used to format the returned message.</p>
     */
    public String localizeMessage(String key, Object[] arguments) {
        List list = new ArrayList();
        for (int x = 0; x < arguments.length; x++) list.add(arguments[x]);
        return this.getText(key, key, list);
    }
}
