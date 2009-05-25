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
package it.could.util.encoding;

/**
 * <p>An interface containing all the character classes used for encoding the
 * different parts of an URI, as specified by
 * <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC-2396</a>.</p>
 *
 * @author <a href="http://could.it/">Pier Fumagalli</a>
 */
public interface URICharacters {
    
    /* ====================================================================== */
    /* CHARACTER CLASSES                                                      */
    /* ====================================================================== */

    /** <p>The alphabet of lower-case characters (RFC-2396 Section 1.6).</p> */
    public static final char CLASS_LOWER_CASE_ALPHA[] = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    /** <p>The alphabet of upper-case characters (RFC-2396 Section 1.6).</p> */
    public static final char CLASS_UPPER_CASE_ALPHA[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    /** <p>The union of upper and lower case letters (RFC-2396 Section 1.6).</p> */
    public static final char CLASS_ALPHA[] = new StringBuffer()
                                                 .append(CLASS_LOWER_CASE_ALPHA)
                                                 .append(CLASS_UPPER_CASE_ALPHA)
                                                 .toString().toCharArray();
    
    /** <p>All numerical digits (RFC-2396 Section 1.6).</p> */
    public static final char CLASS_DIGIT[] = "0123456789".toCharArray();

    /** <p>The union of all letters and digits (RFC-2396 Section 1.6).</p> */
    public static final char CLASS_ALPHANUMERIC[] = new StringBuffer()
                                                        .append(CLASS_ALPHA)
                                                        .append(CLASS_DIGIT)
                                                        .toString().toCharArray();

    /** <p>Mark characters (RFC-2396 Section 2.2).</p> */
    public static final char CLASS_MARK[] = "-_.!~*'()".toCharArray();

    /** <p>Characters for hexadecimal digits (RFC-2396 Section 2.4.1).</p> */
    public static final char CLASS_HEX[] = new StringBuffer()
                                               .append(CLASS_DIGIT)
                                               .append("abcdefABCDEF")
                                               .toString().toCharArray();

    /** <p>All reserved characters (RFC-2396 Section 1.6).</p> */
    public static final char CLASS_RESERVED[] = ";/?:@&=+$,".toCharArray();

    /** <p>All unreserved characters (RFC-2396 Section 2.3).</p> */
    public static final char CLASS_UNRESERVED[] = new StringBuffer()
                                                      .append(CLASS_ALPHANUMERIC)
                                                      .append(CLASS_MARK)
                                                      .toString().toCharArray();


    /** <p>All characters representable in a URI (RFC-2396 Section 2).</p> */
    public static final char CLASS_URI_CHARACTERS[] = new StringBuffer()
                                                          .append(CLASS_RESERVED)
                                                          .append(CLASS_UNRESERVED)
                                                          .toString().toCharArray();

    /* ====================================================================== */
    /* CHARACTERS ALLOWABLE IN SCHEMAS                                        */
    /* ====================================================================== */

    /** <p>The characters allowed as the start of a scheme (RFC-2396 Section 3.1).</p> */ 
    public static final char SCHEME_START[] = CLASS_ALPHA;

    /** <p>The characters allowed after the first one in a scheme (RFC-2396 Section 3.1).</p> */
    public static final char SCHEME_CHARS[] = new StringBuffer()
                                                  .append(CLASS_ALPHANUMERIC)
                                                  .append("+-.")
                                                  .toString().toCharArray();

    /* ====================================================================== */
    /* CHARACTERS ALLOWABLE IN AUTHORITIES                                    */
    /* ====================================================================== */
    
    /** <p>The characters allowed in an authority user name (RFC-2396 Section 3.2).</p> */
    public static final char AUTHORITY_USER[] = new StringBuffer()
                                                    .append(CLASS_UNRESERVED)
                                                    .append(";&=$,")
                                                    .toString().toCharArray();
    
    /** <p>The characters allowed in an authority password (RFC-2396 Section 3.2).</p> */
    public static final char AUTHORITY_PASS[] = AUTHORITY_USER;

    /** <p>The characters allowed in an authority host name (RFC-2396 Section 3.2).</p> */
    public static final char AUTHORITY_HOST[] = new StringBuffer()
                                                    .append(CLASS_ALPHANUMERIC)
                                                    .append("-.")
                                                    .toString().toCharArray();

    /* ====================================================================== */
    /* CHARACTERS ALLOWABLE IN PATHS                                          */
    /* ====================================================================== */

    /** <p>The characters allowed in a path element (RFC-2396 Section 3.3).</p> */
    public static final char PATH[] = new StringBuffer()
                                          .append(CLASS_UNRESERVED)
                                          .append("/?:@$,")
                                          .toString().toCharArray();

    /* ====================================================================== */
    /* CHARACTERS ALLOWABLE IN QUERY                                          */
    /* ====================================================================== */

    /** <p>The characters allowed in the query (RFC-2396 Section 3.4).</p> */
    public static final char QUERY[] = new StringBuffer()
                                           .append(CLASS_UNRESERVED)
                                           .append("&=")
                                           .toString().toCharArray();

    /* ====================================================================== */
    /* CHARACTERS ALLOWABLE IN PARAMETERS                                     */
    /* ====================================================================== */

    /** <p>The characters allowed in a parameter name (RFC-2396 Section 3.3/3.4).</p> */
    public static final char PARAMETER_NAME[] = new StringBuffer()
                                                    .append(CLASS_UNRESERVED)
                                                    .append(":@$,")
                                                    .toString().toCharArray();

    /** <p>The characters allowed in a parameter value (RFC-2396 Section 3.3/3.4).</p> */
    public static final char PARAMETER_VALUE[] = PARAMETER_NAME;

    /* ====================================================================== */
    /* CHARACTERS ALLOWABLE IN FRAGMENTS                                      */
    /* ====================================================================== */

    /** <p>The characters allowed in a fragment (RFC-2396 Section 4.1).</p> */
    public static final char FRAGMENT[] = new StringBuffer()
                                              .append(CLASS_UNRESERVED)
                                              .append(";/?:@&=$,")
                                              .toString().toCharArray();

    /* ====================================================================== */
    /* CHARACTERS ALLOWABLE IN OPAQUE PARTS                                   */
    /* ====================================================================== */

    /** <p>The characters allowed at the start of an opaque part (RFC-2396 Section 3).</p> */
    public static final char OPAQUE_START[] = new StringBuffer()
                                                  .append(CLASS_UNRESERVED)
                                                  .append(";?:@&=+$,")
                                                  .toString().toCharArray();

    /** <p>The characters allowed after the first one in an opaque part (RFC-2396 Section 3).</p> */
    public static final char OPAQUE_CHARS[] = new StringBuffer()
                                                  .append(OPAQUE_START)
                                                  .append("/")
                                                  .toString().toCharArray();
}
