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
import it.could.util.encoding.EncodingAware;
import it.could.util.encoding.EncodingTools;
import it.could.util.encoding.URICharacters;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>The {@link Authority} class represents the user and host information in
 * a {@link Location}.</p>
 * 
 * @author <a href="http://could.it/">Pier Fumagalli</a>
 */
public class Authority implements Encodable, URICharacters {

    /** <p>A {@link Map} of schemes and their default port number.</p> */
    private static final Map PORTS = new HashMap();
    static {
        PORTS.put("acap",   new Integer( 674));
        PORTS.put("dav",    new Integer(  80));
        PORTS.put("ftp",    new Integer(  21)); 
        PORTS.put("gopher", new Integer(  70));
        PORTS.put("http",   new Integer(  80));
        PORTS.put("https",  new Integer( 443));
        PORTS.put("imap",   new Integer( 143));
        PORTS.put("ldap",   new Integer( 389));
        PORTS.put("mailto", new Integer(  25));
        PORTS.put("news",   new Integer( 119));
        PORTS.put("nntp",   new Integer( 119));
        PORTS.put("pop",    new Integer( 110));
        PORTS.put("rtsp",   new Integer( 554));
        PORTS.put("sip",    new Integer(5060));
        PORTS.put("sips",   new Integer(5061));
        PORTS.put("snmp",   new Integer( 161));
        PORTS.put("telnet", new Integer(  23));
        PORTS.put("tftp",   new Integer(  69));
    }

    /** <p>The port number when no port was specified.</p> */
    public static final int UNSPECIFIED_PORT = -1;

    /** <p>The user name of this instance (decoded).</p> */
    private final String user;
    /** <p>The password of this instance (decoded).</p> */
    private final String pass;
    /** <p>The host name of this instance (decoded).</p> */
    private final String host;
    /** <p>The port number of this instance.</p> */
    private final int port;
    /** <p>The encoded string representation of this instance.</p> */
    private final String string;

    /**
     * <p>Create a new {@link Authority} instance given its components.</p>
     * 
     * @throws NullPointerException if the specified host was <b>null</b>.
     * @throws IllegalArgumentException if the specified host was empty or if a
     *                                  password was specified without a user.
     */
    public Authority(String user, String pass, String host, int port)
    throws NullPointerException, IllegalArgumentException {
        /* Checck that the host name is valid */
        if (host == null)  throw new NullPointerException("Null host");
        if (host.length() == 0) 
            throw new IllegalArgumentException("Empty host");
        
        /* Normalize user and pasasword */
        if ((user != null) && (user.length() == 0)) user = null;
        if ((pass != null) && (pass.length() == 0)) pass = null;
        if ((user == null) && (pass != null))
            throw new IllegalArgumentException("Password without user");

        this.user = user;
        this.pass = pass;
        this.host = host;
        this.port = port < 0? UNSPECIFIED_PORT: port;
        this.string = EncodingTools.toString(this);
    }

    /* ====================================================================== */
    /* STATIC CONSTRUCTION METHODS                                            */
    /* ====================================================================== */

    /**
     * <p>Parse a {@link String} into an {@link Authority} structure using the
     * {@link EncodingAware#DEFAULT_ENCODING default encoding}.</p>
     * 
     * <p>The supplied string might look somewhat similar to
     * <code>user:pass@host:port</code> or <code>host</code>.</p>
     *
     * @param auth the {@link String} to parse.
     * @return an {@link Authority} instance or <b>null</b> if the specified
     *         {@link String} was <b>null</b>.
     */
    public static Authority parse(String auth) {
        try {
            return parse(auth, UNSPECIFIED_PORT, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /**
     * <p>Parse a {@link String} into an {@link Authority} structure using the
     * {@link EncodingAware#DEFAULT_ENCODING default encoding}.</p>
     *
     * @see #parse(String, int, String)
     */
    public static Authority parse(String auth, int defaultPort) {
        try {
            return parse(auth, defaultPort, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /**
     * <p>Parse a {@link String} into an {@link Authority} structure.</p>
     *
     * @see #parse(String, int, String)
     */
    public static Authority parse(String auth, String encoding)
    throws UnsupportedEncodingException {
        return parse(auth, UNSPECIFIED_PORT, encoding);
    }

    /**
     * <p>Parse a {@link String} into an {@link Authority} structure.</p>
     * 
     * <p>The supplied string might look somewhat similar to <code>host</code>
     * or <code>user:pass@host:port</code>. If a port was specified in the
     * {@link String} to parse, and it was equivalent to the specified default
     * port, the newly created {@link Authority} instance will return the
     * {@link #UNSPECIFIED_PORT} constant in the {@link #getPort()} method.</p>
     *
     * @param auth the {@link String} to parse.
     * @param defaultPort the default port to strip if found.
     * @param encoding the character encoding to use parsing.
     * @return an {@link Authority} instance or <b>null</b> if the specified
     *         {@link String} was <b>null</b>, empty or malformed.
     * @throws UnsupportedEncodingException if the specified encoding was not
     *                                      supported by the platform.
     */
    public static Authority parse(String auth, int defaultPort, String encoding)
    throws UnsupportedEncodingException {
        /* If the encoding is null, make sure it's defaulted */
        if (encoding == null) encoding = DEFAULT_ENCODING;

        /* If we see a null or empty string to parse, we return a null */
        if (auth == null) return null;
        if (auth.length() == 0) return null;

        /* Split the string in two components using "@" */
        final String split[];
        if (auth.indexOf('@') < 0) {
            split = new String[] { null, auth };
        } else {
            split = StringTools.splitOnce(auth, '@');
        }

        /* If no host info was found we return a null authority (malformed) */
        if (split[1] == null) return null;

        /* Split userinfo and hostinfo into user, pass, host and port */
        final String uinfo[] = StringTools.splitOnce(split[0], ':');
        final String hinfo[] = StringTools.splitOnce(split[1], ':');

        /* If no host part was found we return a null authority (malformed) */
        if (hinfo[0] == null) return null;

        /* If a password was found, but no user, forget the password as well */
        if (uinfo[0] == null) uinfo[1] = null;

        /* Try to parse the port as an integer */
        int port;
        if (hinfo[1] == null) {
            port = UNSPECIFIED_PORT;
        } else try {
            port = Integer.parseInt(hinfo[1]);
            port = defaultPort == port ? UNSPECIFIED_PORT : port;
        } catch (NumberFormatException exception) {
            port = UNSPECIFIED_PORT;
        }

        /* Create a new Authority instance from the parsed components */
        return new Authority(EncodingTools.urlDecode(uinfo[0], encoding),
                             EncodingTools.urlDecode(uinfo[1], encoding),
                             EncodingTools.urlDecode(hinfo[0], encoding),
                             port);
    }

    /* ====================================================================== */
    /* PUBLIC EXPOSED METHODS                                                 */
    /* ====================================================================== */

    /**
     * <p>Merge the user details of this instance together with those of the
     * specified {@link Authority} one and return an new {@link Authority}.</p>
     * 
     * <p>Any field specified in the specified {@link Authority} instance will
     * override any field of this instance.</p>
     */
    public Authority merge(Authority authority) {
        if (authority == null) return this;
        final String user = authority.user != null? authority.user: this.user;
        final String pass = authority.pass != null? authority.pass: this.pass;
        final String host = authority.host;
        final int port = authority.port != UNSPECIFIED_PORT ?
                         authority.port : this.port;
        return new Authority(user, pass, host, port);
    }

    /**
     * <p>Returns the default port number associated with the specified
     * {@link String} scheme ort {@link #UNSPECIFIED_PORT}.</p>
     */
    public static int getPort(String scheme) {
        final Integer port = (Integer) PORTS.get(scheme);
        if (port == null) return UNSPECIFIED_PORT;
        return port.intValue();
    }

    /**
     * <p>Check if this {@link Authority} has the same host name and port
     * number of the one specified.</p>
     */
    public boolean isAuthoritative(Authority authority) {
        if (authority == null) return false;
        return this.port == authority.port && this.host.equals(authority.host);
    }

    /**
     * <p>Returns the decoded user name.</p> 
     */
    public String getUser() {
        return this.user;
    }
    
    /**
     * <p>Returns the decoded password.</p> 
     */
    public String getPass() {
        return this.pass;
    }

    /**
     * <p>Returns the decoded host name.</p> 
     */
    public String getHost() {
        return this.host;
    }

    /**
     * <p>Returns the port number.</p> 
     */
    public int getPort() {
        return this.port;
    }

    /**
     * <p>Return the URL-encoded {@link String} representation of this
     *  instance.</p>
     */
    public String toString() {
        return this.string;
    }

    /**
     * <p>Return the URL-encoded {@link String} representation of this
     *  instance using the specified
     * character encoding.</p>
     */
    public String toString(String encoding)
    throws UnsupportedEncodingException {
        final StringBuffer buffer = new StringBuffer();
        if (this.user != null) {
            buffer.append(EncodingTools.uriEncode(this.user, encoding,
                                                    AUTHORITY_USER));
            if (this.pass != null) {
                buffer.append(':');
                buffer.append(EncodingTools.uriEncode(this.pass, encoding, 
                                                        AUTHORITY_PASS));
            }
            buffer.append('@');
        }

        buffer.append(EncodingTools.uriEncode(this.host, encoding, 
                                                AUTHORITY_HOST));
        if (this.port != UNSPECIFIED_PORT) buffer.append(':').append(port);
        return buffer.toString();
    }

    /**
     * <p>Return the hash code value for this {@link Authority} instance.</p>
     */
    public int hashCode() {
        return this.string.hashCode();
    }

    /**
     * <p>Check if the specified {@link Object} is equal to this
     * {@link Authority} instance.</p>
     */
    public boolean equals(Object object) {
        if (this == object) return true;
        if ((object == null) || (!(object instanceof Authority))) return false;
        Authority a = (Authority) object;
        return (this.user == null? a.user == null: this.user.equals(a.user)) &&
               (this.pass == null? a.pass == null: this.pass.equals(a.pass)) &&
               (this.host.equals(a.host)) &&
               (this.port == a.port);
    }
}