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
package it.could.util;

import it.could.util.encoding.Encodable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p>An utility class providing various static methods operating on
 * {@link String}s.</p>
 * 
 * <p>This class implement the {@link Encodable} interface from which it
 * inherits its {@link Encodable#DEFAULT_ENCODING default encoding}.</p>
 *
 * @author <a href="http://could.it/">Pier Fumagalli</a>
 */
public final class StringTools {

    /** <p>The {@link SimpleDateFormat} RFC-822 date format.</p> */
    private static final String FORMAT_822 = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
    /** <p>The {@link SimpleDateFormat} RFC-822 date format.</p> */
    private static final String FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    /** <p>The {@link TimeZone} to use for dates.</p> */
    private static final TimeZone TIMEZONE = TimeZone.getTimeZone("GMT");
    /** <p>The {@link Locale} to use for dates.</p> */
    private static final Locale LOCALE = Locale.US;

    /** <p>Deny construction.</p> */
    private StringTools() { }

    /* ====================================================================== */
    /* NUMBER AND DATE PARSING AND FORMATTING                                 */
    /* ====================================================================== */

    /**
     * <p>Format a {@link Number} into a {@link String} making sure that
     * {@link NullPointerException}s are not thrown.</p>
     * 
     * @param number the {@link Number} to format.
     * @return a {@link String} instance or <b>null</b> if the object was null.
     */
    public static String formatNumber(Number number) {
        if (number == null) return null;
        return (number.toString());
    }

    /**
     * <p>Parse a {@link String} into a {@link Long}.</p>
     * 
     * @param string the {@link String} to parse.
     * @return a {@link Long} instance or <b>null</b> if the date was null or
     *         if there was an error parsing the specified {@link String}.
     */
    public static Long parseNumber(String string) {
        if (string == null) return null;
        try {
            return new Long(string);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * <p>Format a {@link Date} according to the HTTP/1.1 RFC.</p>
     * 
     * @param date the {@link Date} to format.
     * @return a {@link String} instance or <b>null</b> if the date was null.
     */
    public static String formatHttpDate(Date date) {
        if (date == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_822, LOCALE);
        formatter.setTimeZone(TIMEZONE);
        return formatter.format(date);
    }

    /**
     * <p>Format a {@link Date} according to the ISO 8601 specification.</p>
     * 
     * @param date the {@link Date} to format.
     * @return a {@link String} instance or <b>null</b> if the date was null.
     */
    public static String formatIsoDate(Date date) {
        if (date == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_ISO, LOCALE);
        formatter.setTimeZone(TIMEZONE);
        return formatter.format(date);
    }

    /**
     * <p>Parse a {@link String} into a {@link Date} according to the
     * HTTP/1.1 RFC (<code>Mon, 31 Jan 2000 11:59:00 GMT</code>).</p>
     * 
     * @param string the {@link String} to parse.
     * @return a {@link Date} instance or <b>null</b> if the date was null or
     *         if there was an error parsing the specified {@link String}.
     */
    public static Date parseHttpDate(String string) {
        if (string == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_822, LOCALE);
        formatter.setTimeZone(TIMEZONE);
        try {
            return formatter.parse(string);
        } catch (ParseException exception) {
            return null;
        }
    }

    /**
     * <p>Parse a {@link String} into a {@link Date} according to the ISO 8601
     * specification (<code>2000-12-31T11:59:00Z</code>).</p>
     * 
     * @param string the {@link String} to parse.
     * @return a {@link Date} instance or <b>null</b> if the date was null or
     *         if there was an error parsing the specified {@link String}.
     */
    public static Date parseIsoDate(String string) {
        if (string == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_ISO, LOCALE);
        formatter.setTimeZone(TIMEZONE);
        try {
            return formatter.parse(string);
        } catch (ParseException exception) {
            return null;
        }
    }

    /* ====================================================================== */
    /* STRING SPLITTING                                                       */
    /* ====================================================================== */

    /**
     * <p>Split the specified {@link String} in an array of exactly two
     * {@link String}s according to the specified delimiter, and any resulting
     * element of zero length will be converted to <b>null</b>.</p>
     * 
     * <p>If no delimiter is found in the source {@link String}, then this
     * method will return always an array of two {@link String}s, and the source
     * will be stored in the returned array at offzet zero.</p>
     * 
     * @param source the {@link String} to split.
     * @param delimiter the delimiter character to split on.
     * @return always an array of exactly two {@link String}s.
     */
    public static String[] splitOnce(String source, char delimiter) {
        if (source == null) return new String[] { null, null };
        if (source.length() == 0) return new String[] { null, null };

        final int position = source.indexOf(delimiter);
        if (position < 0) { // --> source
            return new String[] { source, null };
        } else if (position == 0) {
            if (source.length() == 1) { // --> |
                return new String[] { null, null };
            } else { // --> |second
                return new String[] { null, source.substring(1) };
            }
        } else {
            final String first = source.substring(0, position);
            if (source.length() -1 == position) { // --> first|
                return new String[] { first, null };
            } else { // --> first|second
                return new String[] { first, source.substring(position + 1) };
            }
        }
    }

    /**
     * <p>Split the specified {@link String} in an array of {@link String}s
     * according to the specified delimiter, and any resulting element of zero
     * length will be converted to <b>null</b>.</p>
     * 
     * <p>If no delimiter was found in the source {@link String}, then this
     * mehtod will return an array of only one {@link String} containing only
     * the source {@link String} unmodified.</p>
     */
    public static String[] splitAll(String sourcex, char delimiter) {
        if (sourcex == null) return new String[0];
        if (sourcex.length() == 0) return new String[0];

        final List strings = new ArrayList();
        String current = sourcex;
        while (true) {
            final int position = current.indexOf(delimiter);
            if (position < 0) { // --> current
                strings.add(current);
                break;
            } else if (position == 0) {
                if (current.length() == 1) { // --> |
                    strings.add(null);
                    strings.add(null);
                    break;
                } else { // --> |next
                    strings.add(null);
                    current = current.substring(1);
                    continue;
                }
            } else { // --> first|....
                strings.add(current.substring(0, position));
                if (current.length() -1 == position) { // --> first|
                    strings.add(null);
                    break;
                } else { // --> first|second
                    current = current.substring(position + 1);
                    continue;
                }
            }
        }

        return (String []) strings.toArray(new String[strings.size()]);
    }

    /* ====================================================================== */
    /* CHARACTER FINDING                                                      */
    /* ====================================================================== */

    /**
     * <p>Find the first occurrence of one of the specified delimiter characters
     * in the specified source string.</p>
     */
    public static int findFirst(String source, String delimiters) {
        final char array[] = source.toCharArray();
        final char delim[] = delimiters.toCharArray();
        for (int x = 0; x < array.length; x ++) {
            for (int y = 0; y < delim.length; y ++) {
                if (array[x] == delim[y]) return x;
            }
        }
        return -1;
    }

}
