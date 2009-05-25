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
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * <p>The {@link Parameters} class represents an immutable and never empty
 * {@link List} of {@link Parameter} instances.</p>
 *
 * @author <a href="http://could.it/">Pier Fumagalli</a>
 */
public class Parameters extends AbstractList
implements Encodable, URICharacters {

    /** <p>All the {@link Parameter}s in order.</p> */
    private final Parameter parameters[];
    /** <p>The {@link Map} view over all parameters (names are keys).</p> */
    private final Map map;
    /** <p>The {@link Set} of all parameter names.</p> */
    private final Set names;
    /** <p>The encoded {@link String} representation of this.</p> */
    private final String string;

    /**
     * <p>Create a new {@link Parameters} instance from a {@link List} of
     * {@link Parameter} instances.</p>
     * 
     * @throws NullPointerExceptoin if the {@link List} or any of its
     *                              elements waere <b>null</b>.
     * @throws IllegalArgumentException if the {@link List} was empty.
     * @throws ClassCastException if any of the elements in the {@link List} was
     *                            not a {@link Parameter}.
     */
    public Parameters(List parameters) {
        if (parameters == null) throw new NullPointerException("Null list");
        if (parameters.size() == 0)
            throw new IllegalArgumentException("Empty list");

        final Parameter array[] = new Parameter[parameters.size()];
        final Map map = new HashMap();
        for (int x = 0; x < array.length; x ++) {
            final Parameter param = (Parameter) parameters.get(x);
            if (param == null) throw new NullPointerException("Null parameter");
            final String key = param.getName();
            List values = (List) map.get(key);
            if (values == null) {
                values = new ArrayList();
                map.put(key, values);
            }
            values.add(param.getValue());
            array[x] = param;
        }

        /* Make all parameter value lists unmodifiable */
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext(); ) {
            final Map.Entry entry = (Map.Entry) iter.next();
            final List list = (List) entry.getValue();
            entry.setValue(Collections.unmodifiableList(list));
        }

        /* Store the current values */
        this.map = Collections.unmodifiableMap(map);
        this.names = Collections.unmodifiableSet(map.keySet());
        this.parameters = array;
        this.string = EncodingTools.toString(this);
    }

    /* ====================================================================== */
    /* STATIC CONSTRUCTION METHODS                                            */
    /* ====================================================================== */

    /**
     * <p>Utility method to create a new {@link Parameters} instance from a
     * {@link List} of {@link Parameter} instances removing any duplicate.</p>
     *
     * @see #create(List, boolean)
     * @return a <b>non-null</b> and {@link Parameters} instance or <b>null</b>
     *         if the specified {@link List} was <b>null</b> or empty.
     */
    public static Parameters create(List parameters) {
        return create(parameters, true);
    }

    /**
     * <p>Utility method to create a new {@link Parameters} instance from a
     * {@link List} of {@link Parameter} instances.</p>
     * 
     * <p>Any item in the specified {@link List} that is not a {@link Parameter}
     * instance will be ignored by this method.</p> 
     *
     * @param removeDuplicates whether duplicate {@link Parameter}s are removed
     *                         from the returned {@link Parameters} or not.
     * @return a <b>non-null</b> and {@link Parameters} instance or <b>null</b>
     *         if the specified {@link List} was <b>null</b> or empty.
     */
    public static Parameters create(List parameters, boolean removeDuplicates) {
        if (parameters == null) return null;
        if (parameters.size() == 0) return null;

        final List list = new ArrayList();
        for (Iterator iter = parameters.iterator(); iter.hasNext(); ) {
            Object next = iter.next();
            if (next == null) continue;
            if (! (next instanceof Parameter)) continue;
            if (removeDuplicates && list.contains(next)) continue;
            list.add(next);
        }

        if (list.size() == 0) return null;
        return new Parameters(list);
    }

    /* ====================================================================== */

    /**
     * <p>Parse a {@link String} into a {@link Parameters} structure.</p>
     *
     * @see #parse(String, String)
     * @return a <b>non-null</b> and {@link Parameters} instance or <b>null</b>
     *         if the specified {@link String} was <b>null</b> or empty.
     */
    public static Parameters parse(String parameters) {
        try {
            return parse(parameters, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /**
     * <p>Parse a {@link String} into a {@link Parameters} structure.</p>
     *
     * @return a <b>non-null</b> and {@link Parameters} instance or <b>null</b>
     *         if the specified {@link String} was <b>null</b> or empty.
     */
    public static Parameters parse(String parameters, String encoding)
    throws UnsupportedEncodingException {
        if (parameters == null) return null;
        if (parameters.length() == 0) return null;
        if (encoding == null) encoding = DEFAULT_ENCODING;

        final String split[] = StringTools.splitAll(parameters, '&');
        final List list = new ArrayList();
        for (int x = 0; x < split.length; x ++) {
            final Parameter parameter = Parameter.parse(split[x], encoding);
            if (parameter != null) list.add(parameter); 
        }
        if (list.size() == 0) return null;

        return new Parameters(list);
    }

    /* ====================================================================== */
    /* PUBLIC EXPOSED METHODS                                                 */
    /* ====================================================================== */

    /**
     * <p>Return the number of {@link Parameter}s contained in this
     * instance.</p>
     */
    public int size() {
        return this.parameters.length;
    }

    /**
     * <p>Return the {@link Parameter} stored by this instance at the specified
     * index.</p>
     */
    public Object get(int index) {
        return this.parameters[index];
    }

    /**
     * <p>Return an immutable {@link Set} of {@link String}s containing all
     * known {@link Parameter} {@link Parameter#getName() names}.</p>
     */
    public Set getNames() {
        return this.names;
    }

    /**
     * <p>Check if this instance contains the specified parameter.</p> 
     */
    public boolean hasParameter(String name) {
        return this.map.containsKey(name);
    }

    /**
     * <p>Return the first {@link String} value associated with the
     * specified parameter name, or <b>null</b>.</p> 
     */
    public String getValue(String name) {
        final List values = (List) this.map.get(name);
        return values == null ? null : (String) values.get(0);
    }

    /**
     * <p>Return an immutable {@link List} of all {@link String} values
     * associated with the specified parameter name, or <b>null</b>.</p> 
     */
    public List getValues(String name) {
        return (List) this.map.get(name);
    }

    /* ====================================================================== */
    /* OBJECT METHODS                                                         */
    /* ====================================================================== */

    /**
     * <p>Return the URL-encoded {@link String} representation of this
     * {@link Parameters} instance.</p>
     */
    public String toString() {
        return this.string;
    }

    /**
     * <p>Return the URL-encoded {@link String} representation of this
     * {@link Parameters} instance using the specified character encoding.</p>
     */
    public String toString(String encoding)
    throws UnsupportedEncodingException {
        StringBuffer buffer = new StringBuffer();
        for (int x = 0; x < this.parameters.length; x ++) {
            buffer.append('&').append(this.parameters[x].toString(encoding));
        }
        return buffer.substring(1);
    }

    /**
     * <p>Return the hash code value of this {@link Parameters} instance.</p>
     * 
     * <p>The hash code for a {@link Parameters} instance is equal to the
     * hash code of its {@link #toString() string value}.</p>
     */
    public int hashCode() {
        return this.string.hashCode();
    }

    /**
     * <p>Check if the specified {@link Object} is equal to this
     * {@link Parameters} instance.</p>
     * 
     * <p>The specified {@link Object} is considered equal to this one if
     * it is <b>non-null</b>, is a {@link Parameters} instance and all of the
     * {@link Parameter} it contains are equal and in the same order.</p>
     */
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null) return false;
        if (object instanceof Parameters) return super.equals(object);
        return false;
    }
}
