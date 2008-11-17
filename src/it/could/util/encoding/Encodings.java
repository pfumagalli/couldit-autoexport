package it.could.util.encoding;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/** Utility class to create the final fields of the EncodingAware interface */
public class Encodings {
    public static String get(String encoding) {
        final ByteArrayOutputStream s = new ByteArrayOutputStream();
        if (encoding == null) return new OutputStreamWriter(s).getEncoding();
        try {
            return new OutputStreamWriter(s, encoding).getEncoding();
        } catch (UnsupportedEncodingException exception) {
            final String message = "Default encoding \"" + encoding +
                                   "\" not supported by the platform";
            throw (Error) new InternalError(message).initCause(exception);
        }
    }
}
