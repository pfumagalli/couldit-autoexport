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
 * <p>The {@link PathElement} class represents a single path element within
 * a {@link Path} structure.</p>
 *
 * @author <a href="http://could.it/">Pier Fumagalli</a>
 */
public class PathElement implements Encodable {

    /** <p>The name of this path element (decoded).</p> */
    private final String name;
    /** <p>The parameters of this path element (decoded).</p> */
    private final Parameters parameters;
    /** <p>The {@link String} representation of this (encoded).</p> */
    private final String string;

    /**
     * <p>Create a new {@link PathElement} instance.</p>
     * 
     * @throws NullPointerException if the specified name was <b>null</b>.
     * @throws IllegalArgumentException if the specified name was empty.
     */ 
    public PathElement(String name) {
        this(name, null);
    }

    /**
     * <p>Create a new {@link PathElement} instance.</p>
     * 
     * @throws NullPointerException if the specified name was <b>null</b>.
     * @throws IllegalArgumentException if the specified name was empty.
     */ 
    public PathElement(String name, Parameters parameters) {
        if (name == null) throw new NullPointerException("Null path name");
        if (name.length() == 0)
            throw new IllegalArgumentException("Empty path name");

        this.name = name;
        this.parameters = parameters;
        this.string = EncodingTools.toString(this);
    }

    /* ================================================================== */

    /**
     * <p>Parse a {@link String} into an {@link PathElement} structure.</p>
     *
     * @see #parse(String, String)
     * @return a {@link PathElement} instance or <b>null</b> if the specified
     *         {@link String} was <b>null</b> or empty.
     */
    public static PathElement parse(String path) {
        try {
            return parse(path, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /**
     * <p>Parse a {@link String} into an {@link PathElement} structure.</p>
     * 
     * @return a {@link PathElement} instance or <b>null</b> if the specified
     *         {@link String} was <b>null</b> or empty.
     */
    public static PathElement parse(String path, String encoding)
    throws UnsupportedEncodingException {
        if (path == null) return null;
        if (path.length() == 0) return null;
        if (encoding == null) encoding = DEFAULT_ENCODING;
        String split[] = StringTools.splitOnce(path, ';');
        final String name = split[0] == null ? "." :
                            EncodingTools.urlDecode(split[0], encoding);
        final Parameters parameters = Parameters.parse(split[1], encoding);
        return new PathElement(name, parameters);
    }

    /* ================================================================== */
    /* PUBLIC EXPOSED METHODS                                             */
    /* ================================================================== */

    /**
     * <p>Return the URL-decoded name of this {@link PathElement}.</p>
     *
     * @return a <b>non-null</b> {@link String}.
     */
    public String getName() {
        return this.name;
    }

    /**
     * <p>Return the {@link Parameters} instance associated with this
     * {@link PathElement}.</p>
     *
     * @return a {@link Parameters} instance or <b>null</b>.
     */
    public Parameters getParameters() {
        return this.parameters;
    }

    /**
     * <p>Return the URL-decoded {@link String} extra path of this
     * {@link PathElement}.</p>
     * 
     * <p>The extra path of a {@link PathElement} is the {@link String}
     * immediately following the first &quot;<code>!</code>&quot; (exclamation
     * mark) in its {@link #getName() name}.</p>
     *
     * @return a {@link String} instance or <b>null</b>
     */
    public String getExtra() {
        return StringTools.splitOnce(this.name, '!')[1];
    }

    /* ================================================================== */
    /* OBJECT METHODS                                                     */
    /* ================================================================== */

    /**
     * <p>Return the URL-encoded {@link String} representation of this
     * {@link PathElement} instance.</p>
     */
    public String toString() {
        return this.string;
    }

    /**
     * <p>Return the URL-encoded {@link String} representation of this
     * {@link PathElement} instance using the specified character encoding.</p>
     */
    public String toString(String encoding)
    throws UnsupportedEncodingException {
        final String encoded = EncodingTools.uriEncode(this.name, encoding,
                                                       URICharacters.PATH); 
        if (this.parameters == null) {
            return encoded;
        } else {
            final StringBuffer buffer = new StringBuffer();
            buffer.append(encoded);
            buffer.append(';');
            buffer.append(this.parameters.toString(encoding));
            return buffer.toString();
        }
    }

    /**
     * <p>Return the hash code value of this {@link PathElement} instance.</p>
     * 
     * <p>The hash code for a {@link PathElement} instance is equal to the
     * hash code of its {@link #toString() string value}.</p>
     */
    public int hashCode() {
        return this.string.hashCode();
    }

    /**
     * <p>Check if the specified {@link Object} is equal to this
     * {@link PathElement} instance.</p>
     * 
     * <p>The specified {@link Object} is considered equal to this one if
     * it is <b>non-null</b>, is a {@link PathElement} instance and both its
     * {@link #getName() name} and {@link #getParameters() parameters} are
     * equal.</p>
     */
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null) return false;
        if (object instanceof PathElement) {
            final PathElement element = (PathElement) object;
            final boolean check = this.parameters == null ?
                                  element.parameters == null :
                                  this.parameters.equals(element.parameters);
            return this.name.equals(element.name) && check;
        }
        return false;
    }
}