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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>An utility class providing various static methods operating on
 * {@link InputStream input} and {@link OutputStream output} streams.</p>
 *
 * @author <a href="http://could.it/">Pier Fumagalli</a>
 */
public final class StreamTools {

    /** <p>Deny construction.</p> */
    private StreamTools() { };

    /**
     * <p>Copy every byte from the specified {@link InputStream} to the specifed
     * {@link OutputStream} and then close both of them.</p>
     * 
     * <p>This method is equivalent to a call to the following method:
     * {@link #copy(InputStream,OutputStream,boolean) copy(in, out, true)}.</p>
     * 
     * @param in the {@link InputStream} to read bytes from.
     * @param out the {@link OutputStream} to write bytes to.
     * @return the number of bytes copied.
     * @throws IOException if an I/O error occurred copying the data.
     */
    public static long copy(InputStream in, OutputStream out)
    throws IOException {
        return copy(in, out, true);
    }

    /**
     * <p>Copy every byte from the specified {@link InputStream} to the specifed
     * {@link OutputStream} and then optionally close both of them.</p>
     * 
     * @param in the {@link InputStream} to read bytes from.
     * @param out the {@link OutputStream} to write bytes to.
     * @param close whether to close the streams or not.
     * @return the number of bytes copied.
     * @throws IOException if an I/O error occurred copying the data.
     */
    public static long copy(InputStream in, OutputStream out, boolean close)
    throws IOException {
        if (in == null) throw new NullPointerException("Null input");
        if (out == null) throw new NullPointerException("Null output");

        final byte buffer[] = new byte[4096];
        int length = -1;
        long total = 0;
        while ((length = in.read(buffer)) >= 0) {
            out.write(buffer, 0, length);
            total += length;
        }
        
        if (close) {
            in.close();
            out.close();
        }

        return total;
    }
}
