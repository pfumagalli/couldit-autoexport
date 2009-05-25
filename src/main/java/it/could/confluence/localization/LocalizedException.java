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

/**
 * <p>The {@link LocalizedException} represent an exception which can be
 * constructed obtaining messages either from a {@link LocalizedResource} or
 * from a {@link LocalizationHelper} instance.</p>
 */
public class LocalizedException extends Exception {

    public LocalizedException(LocalizedResource resource, String key) {
        this(resource, key, (Throwable) null);
    }

    public LocalizedException(LocalizedResource resource, String key, Object argument) {
        this(resource, key, new Object[] { argument }, (Throwable) null);
    }

    public LocalizedException(LocalizedResource resource, String key, Object arguments[]) {
        this(resource, key, arguments, (Throwable) null);
    }

    public LocalizedException(LocalizedResource resource, String key, Object argument, Throwable cause) {
        this(resource, key, new Object[] { argument }, cause);
    }

    public LocalizedException(LocalizedResource resource, String key, Throwable cause) {
        super(resource.localizeMessage(key), cause);
    }

    public LocalizedException(LocalizedResource resource, String key, Object arguments[], Throwable cause) {
        super(resource.localizeMessage(key, arguments), cause);
    }

}
