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
package it.could.util.location;

import it.could.util.StringTools;
import it.could.util.encoding.Encodable;
import it.could.util.encoding.EncodingTools;
import it.could.util.encoding.URICharacters;

import java.io.UnsupportedEncodingException;

/**
 * <p>The {@link Parameter} class represents a single parameter parsed either
 * from a {@link Location} query string or from a {@link PathElement}.</p>
 * 
 * @author <a href="http://could.it/">Pier Fumagalli</a>
 */
public class Parameter implements Encodable, URICharacters {

    /** <p>The name of the parameter (decoded).</p> */
    private final String name;
    /** <p>The value of the parameter (decoded).</p> */
    private final String value;
    /** <p>The encoded {@link String} representation of this.</p> */
    private final String string;

    /**
     * <p>Create a new {@link Parameter} given a name and a value.</p>
     * 
     * @throws NullPointerException if the name was <b>null</b>.
     * @throws IllegalArgumentException if the name was an empty string.
     */
    public Parameter(String name, String value) {
        if (name == null) throw new NullPointerException("Null name");
        if (name.length() == 0)
            throw new IllegalArgumentException("Empty name");
        if ((value != null) && (value.length() == 0)) value = null;
        this.name = name;
        this.value = value;
        this.string = EncodingTools.toString(this);
    }

    /* ================================================================== */
    /* STATIC CONSTRUCTION METHODS                                        */
    /* ================================================================== */

    /**
     * <p>Parse a {@link String} into a {@link Parameter} structure.</p>
     *
     * @see #parse(String, String)
     * @return a <b>non-null</b> {@link Parameter} instance or <b>null</b> if
     *         the specified string was <b>null</b> or empty.
     */
    public static Parameter parse(String parameter) {
        try {
            return parse(parameter, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /**
     * <p>Parse a {@link String} into a {@link Parameter} structure.</p>
     *
     * @return a <b>non-null</b> {@link Parameter} instance or <b>null</b> if
     *         the specified string was <b>null</b> or empty.
     */
    public static Parameter parse(String parameter, String encoding)
    throws UnsupportedEncodingException {
        if (parameter == null) return null;
        if (parameter.length() == 0) return null;
        if (encoding == null) encoding = DEFAULT_ENCODING;
        String split[] = StringTools.splitOnce(parameter, '=');
        if (split[0] != null) {
            return new Parameter(EncodingTools.urlDecode(split[0], encoding),
                                 EncodingTools.urlDecode(split[1], encoding));
        }
        final String name = EncodingTools.urlDecode(parameter, encoding);
        return new Parameter(name, null);
    }

    /* ================================================================== */
    /* PUBLIC EXPOSED METHODS                                             */
    /* ================================================================== */

    /**
     * <p>Return the URL-decoded name of this {@link Parameter}.</p>
     */
    public String getName() {
        return this.name;
    }

    /**
     * <p>Return the URL-decoded value of this {@link Parameter}.</p>
     */
    public String getValue() {
        return this.value;
    }

    /* ================================================================== */
    /* OBJECT METHODS                                                     */
    /* ================================================================== */

    /**
     * <p>Return the URL-encoded {@link String} representation of this
     * {@link Parameter} instance.</p>
     */
    public String toString() {
        return this.string;
    }

    /**
     * <p>Return the URL-encoded {@link String} representation of this
     * {@link Parameter} instance using the specified character encoding.</p>
     */
    public String toString(String encoding)
    throws UnsupportedEncodingException {
        if (this.value != null) {
            return EncodingTools.uriEncode(this.name, encoding,
                                           PARAMETER_NAME) + "=" +
                   EncodingTools.uriEncode(this.value, encoding,
                                           PARAMETER_VALUE);
        } else {
            return EncodingTools.uriEncode(this.name, encoding,
                                           PARAMETER_NAME);
        }
    }

    /**
     * <p>Return the hash code value for this {@link Parameter} instance.</p>
     * 
     * <p>The hash code for a {@link Parameter} instance is equal to the
     * hash code of its {@link #toString() string value}.</p>
     */
    public int hashCode() {
        return this.string.hashCode();
    }

    /**
     * <p>Check if the specified {@link Object} is equal to this
     * {@link Parameter} instance.</p>
     * 
     * <p>The specified {@link Object} is considered equal to this one if
     * it is <b>non-null</b>, is a {@link Parameter} instance and both its
     * {@link #getName() name} and {@link #getValue() value} are equal.</p>
     */
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null) return false;
        if (object instanceof Parameter) {
            final Parameter param = (Parameter) object;
            final boolean check = this.value == null ? param.value == null :
                                  this.value.equals(param.value);
            return this.name.equals(param.name) && check;
        }
        return false;
   }
}