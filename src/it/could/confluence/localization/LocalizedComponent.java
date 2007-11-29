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

import java.util.Locale;

import org.apache.log4j.Logger;

/**
 * <p>The {@link LocalizedComponent} abstract class represents a generic
 * component providing extended localization functionality.</p>
 */
public abstract class LocalizedComponent implements LocalizedResource {
    
    /** <p>The Log4J {@link Logger} used by this instance.</p> */ 
    protected final Logger log = Logger.getLogger(this.getClass());

    /** <p>The {@link LocalizationHelper} used by this instance.</p> */
    private final LocalizationHelper bundle;

    /**
     * <p>Create a new {@link LocalizedComponent} instance.</p>
     */
    protected LocalizedComponent() {
        this(Locale.getDefault());
    }

    /**
     * <p>Create a new {@link LocalizedComponent} instance.</p>
     */
    protected LocalizedComponent(Locale locale) {
        if (locale == null) locale = Locale.getDefault();
        this.bundle = LocalizationHelper.getBundle(this.getClass(), locale);
    }

    /**
     * <p>Return the localized version of the message identified by the
     * specified key or the default value if the key was not found.</p>
     */
    public String localizeMessage(String key) {
        try {
            return this.bundle.getString(key);
        } catch (Exception exception) {
            return key;
        }
    }

    /**
     * <p>Format the message identified by the specified format key according
     * to the specified array of arguments.</p>
     */
    public String localizeMessage(String key, Object arguments[]) {
        try {
            return this.bundle.formatString(key, arguments);
        } catch (Exception exception) {
            return key;
        }
    }

}
