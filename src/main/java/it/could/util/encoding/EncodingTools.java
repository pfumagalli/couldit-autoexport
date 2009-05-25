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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * <p>An utility class providing various static methods dealing with
 * encodings and {@link Encodable} objects..</p>
 *
 * @author <a href="http://could.it/">Pier Fumagalli</a>
 */
public final class EncodingTools implements EncodingAware, URICharacters {

    /** <p>The array of vaild bytes in a URI.</p> */
    private static final byte URI[];
    /** <p>The Hexadecimap alphabet for URI encoding.</p> */
    private static final byte HEX[];
    static {
        try {
            URI = new String(CLASS_URI_CHARACTERS).getBytes("US-ASCII");
            HEX = "0123456789ABCDEF%".getBytes("US-ASCII");
        } catch (UnsupportedEncodingException exception) {
            final String message = "US-ASCII encoding not supported";
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /** <p>The Base-64 alphabet.</p> */
    private static final char ALPHABET[] = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/', '=' };

    /** <p>Deny construction of this class.</p> */
    private EncodingTools() { }

    /* ====================================================================== */
    /* VARIOUS RANDOM METHODS                                                 */
    /* ====================================================================== */

    /**
     * <p>Return the {@link String} representation of the specified
     * {@link Encodable} object using the {@link EncodingAware#DEFAULT_ENCODING
     * default encoding}.</p>
     *
     * throws NullPointerException if the {@link Encodable} was <b>null</b>.
     */
    public static String toString(Encodable encodable) {
        try {
            return encodable.toString(DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /* ====================================================================== */
    /* URI ENCODING                                                           */
    /* ====================================================================== */

    /**
     * <p>URI-encode the specified string.</p>
     * 
     * <p>This method is similar to {@link #urlEncode(String)}, but it will
     * encode all the non-allowed URI characters, with the <code>allow</code>
     * character array specifying which ones can be preserved.</p>
     * 
     * @throws IllegalArgumentException if any of the character in the
     *                                  <code>allow</code> buffer is not a valid
     *                                  URI character (as in RFC 2396).
     */
    public static String uriEncode(String source, char allow[]) {
        try {
            return uriEncode(source, DEFAULT_ENCODING, allow, allow);
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /**
     * <p>URI-encode the specified string.</p>
     * 
     * <p>This method is similar to {@link #urlEncode(String, String)}, but it
     * will encode all the non-allowed URI characters, with the
     * <code>allow</code> character array specifying which ones can be
     * preserved.</p>
     * 
     * @throws IllegalArgumentException if any of the character in the
     *                                  <code>allow</code> buffer is not a valid
     *                                  URI character (as in RFC 2396).
     */
    public static String uriEncode(String source, String encoding, char allow[])
    throws UnsupportedEncodingException {
        return uriEncode(source, encoding, allow, allow);
    }

    /**
     * <p>URI-encode the specified string.</p>
     * 
     * <p>This method is similar to {@link #urlEncode(String)}, but it will
     * encode all the non-allowed URI characters, with the
     * <code>allowStart</code> and <code>allowChars</code> character arrays
     * specifying which ones can be preserved for the first character and for
     * all remaining ones.</p>
     * 
     * @throws IllegalArgumentException if any of the character in the
     *                                  <code>allow</code> buffer is not a valid
     *                                  URI character (as in RFC 2396).
     */
    public static String uriEncode(String source,
                                   char allowStart[], char allowChars[]) {
        try {
            return uriEncode(source, DEFAULT_ENCODING, allowStart, allowChars);
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /**
     * <p>URI-encode the specified string.</p>
     * 
     * <p>This method is similar to {@link #urlEncode(String, String)}, but it
     * will encode all the non-allowed URI characters, with the
     * <code>allowStart</code> and <code>allowChars</code> character arrays
     * specifying which ones can be preserved for the first character and for
     * all remaining ones.</p>
     * 
     * @throws IllegalArgumentException if any of the character in the
     *                                  <code>allow</code> buffer is not a valid
     *                                  URI character (as in RFC 2396).
     */
    public static String uriEncode(String source, String encoding,
                                   char allowStart[], char allowChars[])
    throws UnsupportedEncodingException {
        if (source == null) return null;
        if (source.length() == 0) return "";
        
        /* Make sure we have an encoding */
        if (encoding == null) encoding = DEFAULT_ENCODING;

        /* Get the encoded bytes representation of the specified string */ 
        final byte bytes[] = source.getBytes(encoding);
        if (bytes.length == 0) return "";

        /*
         * Verify that the character classes given are valid for URIs. Note that
         * for simplicity we are comparing bytes versus characters (normally
         * bad) but in this case we are guaranteed that this is going to work
         * as all the allowable URI characters are US-ASCII between 0 and 127
         * (Unicode Basic Latin).
         */
        if ((allowStart == null) && (allowChars == null)) {
            allowStart = allowChars = new char[0];
        } else {
            for (int x = allowStart.length - 1; x >= 0; x --) {
                int pos = URI.length - 1;
                while ((pos >= 0) && (allowStart[x] != URI[pos])) pos --;
                if (pos < 0) {
                    final String msg = "Invalid URI character ";
                    throw new IllegalArgumentException(msg + allowStart[x]);
                }
            }
            if (allowStart != allowChars) {
                for (int x = allowChars.length - 1; x >= 0; x --) {
                    int pos = URI.length - 1;
                    while ((pos >= 0) && (allowChars[x] != URI[pos])) pos --;
                    if (pos < 0) {
                        final String msg = "Invalid URI character ";
                        throw new IllegalArgumentException(msg + allowChars[x]);
                    }
                }
            }
        }

        /* Create a new char buffer that will hold the encoded bytes */
        byte buffer[] = new byte[3 * bytes.length];
        int bufpos = 1;

        /* Verify the first character against the allowable characters. */
        int pos = allowStart.length - 1;
        while ((pos >= 0) && (allowStart[pos] != bytes[0])) pos --;
        if (pos >= 0) buffer[0] = bytes[0];
        else {
            buffer[0] = HEX[16]; // %
            buffer[1] = HEX[(bytes[0] >> 4) & 0x0F ]; // 0..9..F
            buffer[2] = HEX[(bytes[0]     ) & 0x0F ]; // 0..9..F
            bufpos = 3;
        }

        /* As above, but for all remaining characters */    
        for (int x = 1; x < bytes.length; x ++) {
            final byte cur = bytes[x];
            pos = allowChars.length - 1;
            while ((pos >= 0) && (allowChars[pos] != cur)) pos --;
            if (pos >= 0) buffer[bufpos++] = cur;
            else {
                buffer[bufpos++] = HEX[16]; // %
                buffer[bufpos++] = HEX[(cur >> 4) & 0x0F ]; // 0..9..F
                buffer[bufpos++] = HEX[(cur     ) & 0x0F ]; // 0..9..F
            }
        }

        try {
            return new String(buffer, 0, bufpos, "US-ASCII");
        } catch (UnsupportedEncodingException exception) {
            final String message = "US-ASCII encoding not supported";
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /* ====================================================================== */
    /* URL ENCODING / DECODING                                                */
    /* ====================================================================== */

    /**
     * <p>URL-encode the specified {@link String}.</p>
     * 
     * @see URLEncoder#encode(String, String)
     */
    public static String urlEncode(String source, String encoding)
    throws UnsupportedEncodingException {
        if (source == null) return null;
        if (encoding == null) encoding = DEFAULT_ENCODING;
        return URLEncoder.encode(source, encoding);
    }

    /**
     * <p>URL-encode the specified {@link String} using the
     * {@link EncodingAware#DEFAULT_ENCODING default encoding}.</p>
     * 
     * @see URLEncoder#encode(String, String)
     */
    public static String urlEncode(String source) {
        try {
            return URLEncoder.encode(source, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /**
     * <p>URL-decode the specified {@link String}.</p>
     * 
     * @see URLDecoder#decode(String, String)
     */
    public static String urlDecode(String source, String encoding)
    throws UnsupportedEncodingException {
        if (source == null) return null;
        if (encoding == null) encoding = DEFAULT_ENCODING;
        return URLDecoder.decode(source, encoding);
    }

    /**
     * <p>URL-decode the specified {@link String} using the
     * {@link EncodingAware#DEFAULT_ENCODING default encoding}.</p>
     * 
     * @see URLDecoder#decode(String, String)
     */
    public static String urlDecode(String source) {
        try {
            return URLDecoder.decode(source, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /* ====================================================================== */
    /* BASE 64 ENCODING / DECODING                                            */
    /* ====================================================================== */
    
    /**
     * <p>Encode the specified {@link String} in base 64 using the specified
     * encoding.</p>
     */
    public static final String base64Encode(String string, String encoding)
    throws UnsupportedEncodingException {
        /* Check the source string for null or the empty string. */
        if (string == null) return (null);
        if (string.length() == 0) return "";
    
        /* Check the encoding */
        if (encoding == null) encoding = DEFAULT_ENCODING;
    
        /* Prepare the buffers that we'll use to encode in Base 64 */
        final byte bsrc[] = string.getBytes(encoding);
        final char bdst[] = new char[(bsrc.length + 2) / 3 * 4];
    
        /* Iterate into the source in chunks of three bytes */
        int psrc = -1;
        int pdst = 0;
        int temp = 0;
        while ((psrc = psrc + 3) < bsrc.length) {
            /* For every three bytes processed ... */
            temp = ((bsrc[psrc - 2] << 16) & 0xFF0000) |
                   ((bsrc[psrc - 1] <<  8) & 0x00FF00) |
                   ((bsrc[psrc    ]      ) & 0x0000FF);
            /* ... we append four bytes to the buffer */
            bdst[pdst ++] = ALPHABET[(temp >> 18) & 0x3f];
            bdst[pdst ++] = ALPHABET[(temp >> 12) & 0x3f];
            bdst[pdst ++] = ALPHABET[(temp >>  6) & 0x3f];
            bdst[pdst ++] = ALPHABET[(temp      ) & 0x3f];
        }
    
        /* Let's check whether we still have some bytes to encode */
        switch (psrc - bsrc.length) {
        case 0: /* Two bytes left to encode */
            temp = ((bsrc[psrc - 2] & 0xFF) << 8) | (bsrc[psrc - 1] & 0xFF);
            bdst[pdst ++] = ALPHABET[(temp >> 10) & 0x3f];
            bdst[pdst ++] = ALPHABET[(temp >>  4) & 0x3f];
            bdst[pdst ++] = ALPHABET[(temp <<  2) & 0x3c];
            bdst[pdst ++] = ALPHABET[64];
            break;
        case 1: /* One byte left to encode */
            temp = (bsrc[psrc - 2] & 0xFF);
            bdst[pdst ++] = ALPHABET[(temp >> 2) & 0x3f];
            bdst[pdst ++] = ALPHABET[(temp << 4) & 0x30];
            bdst[pdst ++] = ALPHABET[64];
            bdst[pdst ++] = ALPHABET[64];
        }
    
        /* Convert the character array into a proper string */
        return new String(bdst);
    }

    /**
     * <p>Encode the specified {@link String} in base 64 using the
     * {@link EncodingAware#DEFAULT_ENCODING default encoding}.</p>
     */
    public static final String base64Encode(String string) {
        try {
            return (base64Encode(string, DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /**
     * <p>Decode the specified base 64 {@link String} using the specified
     * encoding.</p>
     */
    public static final String base64Decode(String string, String encoding)
    throws UnsupportedEncodingException {
        /* Check the source string for null or the empty string. */
        if (string == null) return (null);
        if (string.length() == 0) return "";

        /* Check the encoding */
        if (encoding == null) encoding = DEFAULT_ENCODING;

        /* Retrieve the array of characters of the source string. */
        final char characters[] = string.toCharArray();

        /* Check the length, which must be dividible by 4. */
        if ((characters.length & 0x03) != 0)
            throw new IllegalArgumentException("Invalid length for the "+
                    "encoded string (" + characters.length + ")");

        /* The bytes array length is 3/4th of the characters array length */
        byte bytes[] = new byte[characters.length - (characters.length >> 2)];

        /*
         * Since this might take a while check now for the last 4 characters
         * token: it must contain at most two == and those need to be in the
         * last two positions in the array (the only valid sequences are:
         * "????", "???=" and "??==").
         */
        if (((characters[characters.length - 4] == '=') ||
             (characters[characters.length - 3] == '=')) ||
            ((characters[characters.length - 2] == '=') &&
             (characters[characters.length - 1] != '='))) {
            throw new IllegalArgumentException("Invalid pattern for last " +
                    "Base64 token in string to decode: " +
                    characters[characters.length - 4] +
                    characters[characters.length - 3] +
                    characters[characters.length - 2] +
                    characters[characters.length - 1]);
        }

        /* Translate the Base64-encoded String in chunks of 4 characters. */
        int coff = 0;
        int boff = 0;
        while (coff < characters.length) {
            boolean last = (coff == (characters.length - 4));
            int curr = ((value(characters[coff    ], last) << 0x12) |
                        (value(characters[coff + 1], last) << 0x0c) |
                        (value(characters[coff + 2], last) << 0x06) |
                        (value(characters[coff + 3], last)        ));
            bytes[boff + 2] = (byte)((curr        ) & 0xff);
            bytes[boff + 1] = (byte)((curr >> 0x08) & 0xff);
            bytes[boff    ] = (byte)((curr >> 0x10) & 0xff);
            coff += 4;
            boff += 3;
        }

        /* Get the real decoded string length, checking out the trailing '=' */
        if (characters[coff - 1] == '=') boff--;
        if (characters[coff - 2] == '=') boff--;

        /* All done */
        return (new String(bytes, 0, boff, encoding));
    }

    /**
     * <p>Decode the specified base 64 {@link String}  using the
     * {@link EncodingAware#DEFAULT_ENCODING default encoding}.</p>
     */
    public static final String base64Decode(String string) {
        try {
            return (base64Decode(string, DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException exception) {
            final String message = "Unsupported encoding " + DEFAULT_ENCODING;
            throw (Error) new InternalError(message).initCause(exception);
        }
    }

    /* ====================================================================== */

    /** <p>Retrieve the offset of a character in the base 64 alphabet.</p> */
    private static final int value(char character, boolean last) {
        for (int x = 0; x < 64; x++) if (ALPHABET[x] == character) return (x);
        if (last && (character == ALPHABET[64])) return(0);
        final String message = "Character \"" + character + "\" invalid";
        throw new IllegalArgumentException(message);
    }
}
