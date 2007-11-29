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


/**
 * <p>An utility class representing an HTTP-like URL.</p>
 * 
 * <p>This class can be used to represent any URL that roughly uses the HTTP
 * format. Compared to the standard {@link java.net.URL} class, the scheme part
 * of the a {@link Location} is never checked, and it's up to the application
 * to verify its correctness, while compared to the {@link java.net.URI} class,
 * its parsing mechanism is a lot more relaxed (be liberal in what you accept,
 * be strict in what you send).</p>
 * 
 * <p>For a bigger picture on how this class works, this is an easy-to-read
 * representation of what the different parts of a {@link Location} are:</p>
 * 
 * <div align="center">
 *   <a href="url.pdf" target="_new" title="PDF Version">
 *     <img src="url.gif" alt="URL components" border="0">
 *   </a>
 * </div>
 *
 * @author <a href="http://could.it/">Pier Fumagalli</a>
 */
public class Location implements Encodable, URICharacters {
    
    /** <p>A {@link String} for the chars allowable in a scheme start.</p> */
    private static final String SCHEME_START_STR = new String(SCHEME_START);
    /** <p>A {@link String} for the chars allowable in a scheme.</p> */
    private static final String SCHEME_CHARS_STR = new String(SCHEME_CHARS);

    /** <p>The {@link String} scheme of this {@link Location}.</p> */
    private final String scheme;
    /** <p>The {@link Authority} of this {@link Location}.</p> */
    private final Authority authority;
    /** <p>The {@link Path} of this {@link Location}.</p> */
    private final Path path;
    /** <p>The {@link Parameters} of this {@link Location}.</p> */
    private final Parameters parameters;
    /** <p>The fragment part of this {@link Location}.</p> */
    private final String fragment;
    /** <p>The opaque part of this {@link Location}.</p> */
    private final String opaque;
    /** <p>The string representation of this {@link Location}.</p> */
    private final String string;

    /**
     * <p>Create a new opaque {@link Location} instance.</p>
     * 
     * @throws NullPointerException if either the specified scheme or opaque
     *                              part were <b>null</b>.
     * @throws IllegalArgumentException if the specified scheme did not conform
     *                                  to RFC-2396 (URIs).
     */
    public Location(String scheme, String opaquePart) {
        if (scheme == null) throw new NullPointerException("Null scheme");
        if (opaquePart == null)
            throw new NullPointerException("Null opaque part");

        this.scheme = verifyScheme(scheme);
        this.opaque = opaquePart;
        
        this.authority = null;
        this.path = null;
        this.parameters = null;
        this.fragment = null;

        this.string = EncodingTools.toString(this);
    }

    /**
     * <p>Create a new hierarchical {@link Location} instance.</p>
     * 
     * @throws NullPointerException if the {@link Path} was <b>null</b>.
     * @throws IllegalArgumentExcption if an {@link Authority} instance was
     *                                 specified but the scheme was <b>null</b>
     *                                 or if the specified scheme did not
     *                                 conform to RFC-2396 (URIs).
     */
    public Location(String scheme, Authority authority, Path path,
                    Parameters parameters, String fragment) {
        if (path == null) throw new NullPointerException("No path specified");
        if ((scheme == null) && (authority != null))
            throw new IllegalArgumentException("No scheme specified");
        if ((fragment != null) && (fragment.length() == 0)) fragment = null;

        this.scheme = verifyScheme(scheme);
        this.authority = authority;
        this.path = path;
        this.parameters = parameters;
        this.fragment = fragment;

        this.opaque = null;

        this.string = EncodingTools.toString(this);
    }

    /* ====================================================================== */
    /* STATIC CONSTRUCTION METHODS                                            */
    /* ====================================================================== */
    
    /**
     * <p>Parse a {@link String} into a {@link Location} structure using the
     * {@link EncodingAware#DEFAULT_ENCODING default encoding}.</p>
     */
    public static Location parse(String location) {
        try {
            return parse(location, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            final InternalError error = new InternalError(message);
            throw (InternalError) error.initCause(exception);
        }
    }

    /**
     * <p>Parse a {@link String} into a {@link Location} structure using the
     * specified encoding.</p>
     */
    public static Location parse(String location, String encoding)
    throws UnsupportedEncodingException {
        if (location == null) return null;
        if (encoding == null) encoding = DEFAULT_ENCODING;
        final String components[] = parseComponents(location);
        if (components.length == 2)
            return new Location(components[0], components[1]);

        final int port = Authority.getPort(components[0]);
        final Authority auth = Authority.parse(components[1], port, encoding);
        final Path path = Path.parse(components[2], encoding);
        final Parameters params = Parameters.parse(components[3], encoding);
        final String frag = components[4];
        return new Location(components[0], auth, path, params, frag);
    }

    /* ====================================================================== */
    /* ACCESSOR METHODS                                                       */
    /* ====================================================================== */

    /**
     * <p>Return the {@link String} scheme associated with this {@link Location}
     * instance or <b>null</b>.</p>
     */
    public String getScheme() {
        return this.scheme;
    }

    /**
     * <p>Return the {@link Authority} associated with this {@link Location}
     * instance or <b>null</b>.</p>
     */
    public Authority getAuthority() {
        return this.authority;
    }

    /**
     * <p>Return the {@link Path} structure associated with this
     * {@link Location} instance or <b>null</b> if this {@link Location}
     * is {@link #isOpaque() opaque}.</p> 
     */
    public Path getPath() {
        return this.path;
    }

    /**
     * <p>Return the {@link Parameters} parsed from this {@link Location}'s
     * query string or <b>null</b>.</p>
     */
    public Parameters getParameters() {
        return this.parameters;
    }

    /**
     * <p>Return the fragment of this {@link Location} (URL-decoded) or
     * <b>null</b>.</p>
     */
    public String getFragment() {
        return this.fragment;
    }

    /**
     * <p>Return the {@link String} representing the opaque part of this
     * {@link Location} instance or <b>null</b> if this {@link Location}
     * is not {@link #isOpaque() opaque}.</p> 
     */
    public String getOpaquePart() {
        return this.opaque;
    }

    /* ====================================================================== */
    /* OBJECT METHODS                                                         */
    /* ====================================================================== */

    /**
     * <p>Check if the specified {@link Object} is equal to this instance.</p>
     * 
     * <p>The specified {@link Object} must be a <b>non-null</b>
     * {@link Location} instance whose {@link #toString() string value} equals
     * this one's.</p>
     */
    public boolean equals(Object object) {
        if ((object != null) && (object instanceof Location)) {
            return this.string.equals(((Location)object).string);
        } else {
            return false;
        }
    }

    /**
     * <p>Return the hash code value for this {@link Location} instance.</p>
     */
    public int hashCode() {
        return this.string.hashCode();
    }

    /**
     * <p>Return the {@link String} representation of this {@link Location}
     * instance.</p>
     */
    public String toString() {
        return this.string;
    }

    /**
     * <p>Return the {@link String} representation of this {@link Location}
     * instance using the specified character encoding.</p>
     */
    public String toString(String encoding)
    throws UnsupportedEncodingException {
        final StringBuffer buffer = new StringBuffer();
        
        /* Render the scheme (no encoding, as it's verified already) */
        if (this.scheme != null) buffer.append(this.scheme);

        /* Render an opaque location */
        if (this.opaque != null) {
            buffer.append(':');
            buffer.append(EncodingTools.uriEncode(this.opaque,
                                                  encoding,
                                                  OPAQUE_START,
                                                  OPAQUE_CHARS));
            return buffer.toString();
        }

        /* Hierarchical location */
        if (this.scheme != null) buffer.append("://");

        /* Render the authority part */
        if (this.authority != null)
            buffer.append(this.authority.toString(encoding));

        /* Render the paths */
        buffer.append(this.path.toString(encoding));

        /* Render the query string */
        if (this.parameters != null)
            buffer.append('?').append(this.parameters.toString(encoding));

        /* Render the fragment */
        if (this.fragment != null) {
            buffer.append('#');
            buffer.append(EncodingTools.uriEncode(this.fragment,
                                                  encoding,
                                                  FRAGMENT));
        }

        /* Return the string */
        return buffer.toString();
    }

    /* ====================================================================== */
    /* PUBLIC METHODS                                                         */
    /* ====================================================================== */

    /**
     * <p>Checks if this {@link Location} is opaque or not.</p>
     * 
     * <p>If this {@link Location} is opaque, the {@link #getAuthority()},
     * {@link #getPath()}, {@link #getParameters()} and {@link #getFragment()}
     * methods will always return <b>null</b>, while {@link #getOpaquePart()}
     * will return the opaque part of this {@link Location}.</p>
     */
    public boolean isOpaque() {
        return this.opaque != null;
    }

    /**
     * <p>Checks whether this {@link Location} is absolute.</p>
     * 
     * <p>Please do not confuse this method with the {@link Path#isAbsolute()}
     * method in the {@link Path} class. This method will check if the entire
     * {@link Location} is absolute (it is not {@link #isOpaque()} and it has
     * a {@link #getScheme() scheme}.</p>
     *
     * <p>Note that this is <b>not</b> the inverse of the {@link #isRelative()}
     * method.</p>
     */
    public boolean isAbsolute() {
        return this.scheme != null;
    }

    /**
     * <p>Checks whether this {@link Location} is relative.</p>
     * 
     * <p>This method will check if the {@link #getPath() path} enclosed in
     * this {@link Location} instance exists (this instance is not
     * {@link #isOpaque() opaque}) and that it is not
     * {@link Path#isAbsolute() absolute}.</p>
     *
     * <p>Note that this is <b>not</b> the inverse of the {@link #isAbsolute()}
     * method.</p>
     */
    public boolean isRelative() {
        if (this.path == null) return false;
        return ! this.path.isAbsolute();
    }

    /* ====================================================================== */

    /**
     * <p>Checks whether this {@link Location} is authoritative for the
     * specified {@link Location} instance.</p>
     * 
     * <p>Given a {@link Location} instance called <code>x</code> and another
     * one called <code>y</code>, we can write the following table for the
     * expression {@link #isAuthoritative(Location) x.isAuthoritative(y)}:</p>
     * 
     * <table border="1">
     *   <tr>
     *     <td colspan="2" rowspan="2" align="center"></td>
     *     <th bgcolor="#999999" colspan="3">&quot;<code>y</code>&quot;</th>
     *   </tr>
     *   <tr>
     *     <th bgcolor="#cccccc">{@link #isOpaque() opaque}</th>
     *     <th bgcolor="#cccccc">{@link #isAbsolute() absolute}</th>
     *     <th bgcolor="#cccccc"><code>not absolute</code></th>
     *   </tr>
     *   <tr>
     *     <th bgcolor="#999999" rowspan="3">&quot;<code>x</code>&quot;</th>
     *     <th bgcolor="#cccccc">{@link #isOpaque() opaque}</th>
     *     <td bgcolor="#ffcccc" align="center"><i>false</i></td>
     *     <td bgcolor="#ffcccc" align="center"><i>false</i></td>
     *     <td bgcolor="#ffcccc" align="center"><i>false</i></td>
     *   </tr>
     *   <tr>
     *     <th bgcolor="#cccccc">{@link #isAbsolute() absolute}</th>
     *     <td bgcolor="#ffcccc" align="center"><i>false</i></td>
     *     <td bgcolor="#ccccff" align="center">see below...</td>
     *     <td bgcolor="#ccffcc" align="center"><i>true</i></td>
     *   </tr>
     *   <tr>
     *     <th bgcolor="#cccccc"><code>not absolute</code></th>
     *     <td bgcolor="#ffcccc" align="center"><i>false</i></td>
     *     <td bgcolor="#ffcccc" align="center"><i>false</i></td>
     *     <td bgcolor="#ccffcc" align="center"><i>true</i></td>
     *   </tr>
     * </table>
     * 
     * <p>When both <code>x</code> and <code>y</code> are {@link #isAbsolute()
     * absolute}, then their {@link #getScheme() schemes} will be compared for
     * equality, and the following must return true: <br /></p>
     *
     * <p><code>x.{@link #getAuthority() getAuthority}().{@link
     * Authority#isAuthoritative(Authority)
     * isAuthoritative}(y.{@link #getAuthority() getAuthority}())</code></p>
     * 
     * @throws NullPointerException if the {@link Location} was <b>null</b>.
     */
    public boolean isAuthoritative(Location location) {
        if (this.isOpaque() || location.isOpaque()) return false;

        if (! this.isAbsolute()) return ! location.isAbsolute();
        if (! location.isAbsolute()) return true;

        boolean auth = this.authority == null ? location.authority == null :
                       this.authority.isAuthoritative(location.authority);

        return this.scheme.equals(location.scheme) && auth;
    }

    /**
     * <p>Check if this {@link Location} is a parent for the specified one.</p>
     * 
     * <p>To return <b>true</b> this instance must be
     * {@link #isAuthoritative(Location) authoritative} for the one specified,
     * and this instance's {@link #getPath() path} must be a
     * {@link Path#isParent(Path) parent} of the specified {@link Location}'s
     * {@link #getPath() path}.</p>
     * 
     * @throws NullPointerException if the {@link Location} was <b>null</b>.
     */
    public boolean isParent(Location location) {
        if (this.isAuthoritative(location))
            return this.path.isParent(location.path);
        return false;
    }

    /**
     * <p>Check if this {@link Location} identifies the same location of the
     * specified one.</p>
     * 
     * <p>This method differs from {@link #equals(Object)} in terms that it will
     * not check the {@link #getParameters() parameters} and the
     * {@link #getFragment()}, and if the specified {@link Location} is not
     * {@link #isAbsolute() absolute} it will be {@link #resolve(Location)
     * resolved} against this one.</p>
     * 
     * @throws NullPointerException if the {@link Location} was <b>null</b>.
     */
    public boolean isSame(Location location) {
        if (this.isOpaque() || location.isOpaque()) return false;
        if (this.isRelative()) return false;
        
        if (this.isAbsolute()) {
            if (!location.isAbsolute()) location = this.resolve(location);
            return (this.scheme.equals(location.scheme)) &&
                   (this.authority.equals(location.authority)) &&
                   (this.path.equals(location.path));
        } else {
            return this.path.equals(location.path);
        }
    }

    /* ====================================================================== */
    /* RESOLUTION METHODS                                                     */
    /* ====================================================================== */

    /**
     * <p>Parse the specified {@link String} location using the
     * {@link EncodingAware#DEFAULT_ENCODING default character encoding} into a
     * {@link Location} and {@link #resolve(Location) resolve} it against
     * this one.</p>
     * 
     * @see #parse(String)
     * @see #resolve(Location)
     */
    public Location resolve(String location) {
        return this.resolve(parse(location));
    }

    /**
     * <p>Parse the specified {@link String} location using the specified
     * character encoding into a {@link Location} and {@link #resolve(Location)
     * resolve} it against this one.</p>
     * 
     * @see #parse(String, String)
     * @see #resolve(Location)
     */
    public Location resolve(String location, String encoding)
    throws UnsupportedEncodingException {
        return this.resolve(parse(location, encoding));
    }

    /**
     * <p>Resolve the specified {@link Location} against this one.</p>
     * 
     * @return a <b>non-null</b> {@link Location} instance.
     */
    public Location resolve(Location location) {
        if (location == null) return this;

        /* Check that this location is authoritative for the other one */
        if (! this.isAuthoritative(location)) return location;

        /* Authority needs to be merged (for username and password) */
        final Authority auth = this.authority == null ? location.authority :
                               this.authority.merge(location.authority);

        /* Path can be resolved */
        final Path path = this.path.resolve(location.path);

        /* Parameters and fragment are the ones of the target */
        final Parameters params = location.parameters;
        final String fragment = location.fragment;

        /* Create a new {@link Location} instance */
        return new Location(this.scheme, auth, path, params, fragment);
    }

    /* ====================================================================== */
    /* RELATIVIZATION METHODS                                                 */
    /* ====================================================================== */
    
    /**
     * <p>Parse the specified {@link String} into a {@link Location} and
     * relativize it against this one.</p>
     * 
     * <p>This method is equivalent to a call to
     * {@link #relativize(String, boolean) relativize(path, true)}.</p>
     * 
     * @see #relativize(String, boolean)
     * @return a <b>non-null</b> {@link Path} instance.
     */
    public Location relativize(String location) {
        return this.relativize(parse(location), true);
    }

    public Location relativize(String location, boolean allowParent) {
        return this.relativize(parse(location), allowParent);
    }

    public Location relativize(String location, String encoding)
    throws UnsupportedEncodingException {
        return this.relativize(parse(location, encoding), true);
    }

    public Location relativize(String url, String encoding, boolean allowParent)
    throws UnsupportedEncodingException {
        return this.relativize(parse(url, encoding), allowParent);
    }

    public Location relativize(Location location) {
        return this.relativize(location, true);
    }

    public Location relativize(Location location, boolean allowParent) {
        /* Check that this location is authoritative for the other one */
        if (! this.isAuthoritative(location)) return location;

        /* Target location is not absolute, its path might */
        final Path path = this.path.relativize(location.path, allowParent);

        return new Location(null, null, path, location.parameters,
                            location.fragment);
    }

    /* ====================================================================== */
    /* INTERNAL PARSING ROUTINES                                              */
    /* ====================================================================== */

    /**
     * <p>Verify a scheme</p>
     */
    private static String verifyScheme(String scheme) {
        if (scheme == null) return null;
        if (scheme.length() == 0) 
            throw new IllegalArgumentException("Empty scheme");
        if (SCHEME_START_STR.indexOf(scheme.charAt(0)) < 0)
            throw new IllegalArgumentException("Invalid scheme " + scheme);
        for (int x = scheme.length() - 1; x > 0 ; x --) {
            if (SCHEME_CHARS_STR.indexOf(scheme.charAt(x)) >= 0) continue;
            throw new IllegalArgumentException("Invalid scheme " + scheme);
        }
        return scheme.toLowerCase();
    }

    /**
     * <p>Parse <code>scheme://authority/path?query#fragment</code>.</p>
     *
     * @return an array of five {@link String}s: scheme (0), authority (1),
     *         path (2), query (3) and fragment (4).
     */
    private static String[] parseComponents(String url) {

        /* Zero length string is a relative location pointing to nothing */
        if (url.length() == 0) 
            return new String[] { null, null, "", null, null };
        
        /* Check if we start with a "#fragment" or a "?query" */
        final char start = url.charAt(0);
        if (start == '#') {
            return new String[] { null, null, "", null, url.substring(1) };
        } else if (start == '?') {
            final int pos = url.indexOf('#', 1);
            if (pos < 0) {
                return new String[] { null, null, "", url.substring(1), null };
            } else {
                final String query = url.substring(1, pos);
                final String fragm = url.substring(pos + 1);
                return new String[] { null, null, "", query, fragm };
            }
        }

        /* Detect the scheme of the location verfiying its characters */ 
        final String scheme;
        final String afterScheme;
        if (SCHEME_START_STR.indexOf(url.charAt(0)) >= 0) {
            // --> a....
            final int schemeEnd = url.indexOf(':', 1);
            if (schemeEnd < 0) {
                // --> a....
                scheme = null;
                afterScheme = url;
            } else {
                // --> a....:....
                int x = 1;
                while (x < schemeEnd) {
                    if (SCHEME_CHARS_STR.indexOf(url.charAt(x++)) < 0) break;
                }
                if (x < schemeEnd) {
                    // --> sch!eme:....
                    scheme = null;
                    afterScheme = url;
                } else {
                    // --> scheme:...
                    scheme = url.substring(0, schemeEnd);
                    afterScheme = url.substring(schemeEnd + 1);
                }
            }
        } else {
            // --> /path/path...
            scheme = null;
            afterScheme = url;
        }
        
        /* Authority (can be tricky because it can be emtpy) */
        final String auth;
        final String afterAuth;
        if (scheme == null) {
            // --> /path... or path...
            afterAuth = afterScheme;
            auth = null;
        } else if (afterScheme.equals("//")) {
            return new String[] { scheme, afterScheme };

        } else if (afterScheme.startsWith("//")) {
            // --> scheme://...
            final int pathStart = afterScheme.indexOf('/', 2);
            if (pathStart == 2) {
                // --> scheme:///path...
                auth = null;
                afterAuth = afterScheme.substring(pathStart);
            } else if (pathStart > 2) {
                // --> scheme://authority/path...
                afterAuth = afterScheme.substring(pathStart);
                auth = afterScheme.substring(2, pathStart);
            } else {
                // --> scheme://authority (but no slashes for the path)
                final int authEnds = StringTools.findFirst(afterScheme, "?#");
                if (authEnds < 2) {
                    // --> scheme://authority (that's it, return)
                    auth = afterScheme.substring(2);
                    return new String[] { scheme, auth, "/", null, null };
                } else if (authEnds == 2) {
                    return new String[] { scheme, afterScheme };

                } else {
                    // --> scheme://authority?... or scheme://authority#...
                    auth = afterScheme.substring(2, authEnds);
                    afterAuth = "/" + afterScheme.substring(authEnds);
                }
            }
        } else if (afterScheme.startsWith("/")) {
            // --> scheme:/path...
            afterAuth = afterScheme;
            auth = null;

        } else {
            // --> scheme:something...
            return new String[] { scheme, afterScheme };
        }

        /* Path, can be terminated by '?' or '#' whichever is first */
        final int pathEnds = StringTools.findFirst(afterAuth, "?#");
        if (pathEnds < 0) {
            // --> ...path... (no fragment or query, return now)
            return new String[] { scheme, auth, afterAuth, null, null };
        }

        /* We have either a query, a fragment or both after the path */
        final String path = afterAuth.substring(0, pathEnds);
        final String afterPath = afterAuth.substring(pathEnds + 1);

        /* Query? The query can contain a "#" and has an extra fragment */
        if (afterAuth.charAt(pathEnds) == '?') {
            final int fragmPos = afterPath.indexOf('#');
            if (fragmPos < 0) {
                // --> ...path...?... (no fragment)
                return new String[] { scheme, auth, path, afterPath, null };
            }

            // --> ...path...?...#... (has also a fragment)
            final String query = afterPath.substring(1, fragmPos);
            final String fragm = afterPath.substring(fragmPos + 1);
            return new String[] { scheme, auth, path, query, fragm };
        }

        // --> ...path...#... (a path followed by a fragment but no query)
        return new String[] { scheme, auth, path, null, afterPath };
    }
}
