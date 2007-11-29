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
package it.could.confluence;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * <p>A support classes dealing with localization and internationalization of
 * messages using {@link ResourceBundle}s.</p>
 */
public final class LocalizationHelper extends ResourceBundle {

    /** <p>The cache of our {@link ResourceBundle}s.</p> */
    private static final Map CACHE = new HashMap();
    /** <p>An empty enumeration to return in {@link #getKeys()}.</p> */
    private static final Enumeration EMPTY = new Vector().elements();

    /** <p>The nested {@link ResourceBundle} or <b>null</b>.</p> */
    private final ResourceBundle bundle;
    /** <p>The {@link Logger} of the class specified at construction.</p> */
    private final Logger log;

    /**
     * <p>Create a new {@link LocalizationHelper} instance associated with the
     * specified {@link Class} and with the specified {@link Locale}.</p>
     * 
     * <p>Given a class called (for example) <code>mypackage.MyClass</code>,
     * this instance will attempt to locate the properties file in the
     * <code>mypackage/MyClass.properties</code> class loader resource, and
     * the properties file in the <code>mypackage/package.properties</code>
     * class loader resource for shared properties.</p>
     */
    private LocalizationHelper(Class clazz, Locale locale) {
        /* Validate parameters */
        if (clazz == null) throw new NullPointerException("Null class");
        if (locale == null) locale = Locale.getDefault();

        /* Setup the current logger instance */
        this.log = Logger.getLogger(clazz);
        
        /* First of all initialize all that we need for loading bundles */
        final int dot = clazz.getName().lastIndexOf('.');
        final ClassLoader loader = clazz.getClassLoader();
        final String cName = clazz.getName();
        final String pName = dot < 0 ? "" : cName.substring(0, dot + 1);
        final String cRsrc = cName.replace('.', '/');
        final String pRsrc = pName.replace('.', '/') + "package";
        
        /* Try to load the package bundle as the parent of this bundle */
        try {
            this.setParent(ResourceBundle.getBundle(pRsrc, locale, loader));
        } catch (Throwable throwable) {
            if (this.log.isDebugEnabled()) {
                String message = "Cannot load resource bundle " + pRsrc; 
                this.log.debug(message, throwable);
            }
        }
        
        /* Try to load the package bundle as the parent of this bundle */
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle(cRsrc, locale, loader);
        } catch (Throwable throwable) {
            if (this.log.isDebugEnabled()) {
                String message = "Cannot load resource bundle " + cRsrc; 
                this.log.debug(message, throwable);
            }
        } finally {
            this.bundle = bundle;
        }
    }

    /**
     * <p>Create a new {@link LocalizationHelper} instance associated with the
     * specified {@link Class} and with the default {@link Locale#getDefault()
     * Locale}.</p>
     */
    public static LocalizationHelper getBundle(Class clazz) {
        return getBundle(clazz, null);
    }

    /**
     * <p>Create a new {@link LocalizationHelper} instance associated with the
     * specified {@link Class} and with the specified {@link Locale}.</p>
     * 
     * <p>If the specified {@link Locale} was <b>null</b> the default
     * {@link Locale#getDefault() Locale} will be used.</p> 
     */
    public static LocalizationHelper getBundle(Class clazz, Locale locale) {
        if (locale == null) locale = Locale.getDefault();

        /* Look up in the cache for a map associated with the specified class */
        Map byLocale = null;
        synchronized (CACHE) {
            byLocale = (Map) CACHE.get(clazz);
            if (byLocale == null) {
                byLocale = new HashMap();
                CACHE.put(clazz, byLocale);
            }
        }

        /* In the cache we found or we put a map, this has locales as keys */
        LocalizationHelper helper = null;
        synchronized (byLocale) {
            helper = (LocalizationHelper) byLocale.get(locale);
            if (helper == null) {
                helper = new LocalizationHelper(clazz, locale);
                byLocale.put(locale, helper);
            }
        }

        /* Return the cached LocalizationHelper or the one we created */
        return helper;
    }

    /**
     * <p>Retrieve a localized {@link Object} from this bundle.</p>
     */
    protected Object handleGetObject(String key) {
        if (this.bundle == null) return null;
        try {
            return this.bundle.getObject(key);
        } catch (MissingResourceException exception) {
            return null;
        }
    }

    /**
     * <p>Return an {@link Enumeration} of all keys in this bundle.</p>
     */
    public Enumeration getKeys() {
        if (this.bundle == null) return EMPTY;
        return this.bundle.getKeys();
    }

    /**
     * <p>Format the localized {@link String} associated with the specified
     * key, according to the specified parameters.</p>
     * 
     * @param key the key associated with the format key to use.
     * @param args the arguments for formatting the message.
     * @return a formatted message according to the specified parameters.
     */
    public String formatString(String key, Object args[]) {
        try {
            return this.formatStringOrFail(key, args);
        } catch (Exception exception) {
            return key;
        }
    }

    /**
     * <p>Format the localized {@link String} associated with the specified
     * key, according to the specified parameters.</p>
     * 
     * @param key the key associated with the format key to use.
     * @param args the arguments for formatting the message.
     * @return a formatted message according to the specified parameters.
     * @throws MissingResourceException if the specified key was not found
     *                                  in this {@link LocalizationHelper}.
     * @throws IllegalArgumentException if the message could not be formatted.
     */
    public String formatStringOrFail(String key, Object args[]) {
        final String format = this.getString(key);
        return MessageFormat.format(format, args);
    }
}

