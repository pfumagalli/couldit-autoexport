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
package it.could.confluence.autoexport;

import it.could.confluence.autoexport.templates.TemplatesAware;
import it.could.confluence.localization.LocalizedComponent;
import it.could.confluence.localization.LocalizedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.atlassian.confluence.setup.BootstrapManager;
import com.atlassian.confluence.util.ConfluenceVelocityResourceCache;
import com.opensymphony.webwork.views.velocity.VelocityManager;

/**
 * <p>The {@link TemplatesManager} provides a single access point for all
 * Velocity templates operations tied to the AutoExport plugin.</p>
 */
public class TemplatesManager extends LocalizedComponent
implements TemplatesAware {

    /** <p>The encoding used to load, save, and parse templates.</p> */
    public static final String ENCODING = "UTF-8";

    /** <p>The {@link BootstrapManager} used to locate templates.</p> */
    private BootstrapManager bootstrapManager = null;

    /** <p>Deny public construction.</p> */
    TemplatesManager() { }

    /* ====================================================================== */
    /* PRIVATE METHODS TO ACCESS THE FILES UNDERLYING THE TEMPLATES           */
    /* ====================================================================== */

    /**
     * <p>Read bytes from an {@link InputStream} encoding HTML characters.</p>
     */
    private String read(InputStream bytes)
    throws IOException {
        Reader input = null;
        try {
            input = new InputStreamReader(bytes, ENCODING);
            final StringBuffer buffer = new StringBuffer();
            int character = -1;
            while ((character = input.read()) >= 0) {
                switch (character) {
                    case '<': buffer.append("&lt;"); break;
                    case '>': buffer.append("&gt;"); break;
                    case '&': buffer.append("&amp;"); break;
                    case '"': buffer.append("&quot;"); break;
                    default:  buffer.append((char) character);
                }
            }
            return buffer.toString();
        } finally {
            try {
                if (input != null) input.close();
            } finally {
                bytes.close();
            }
        }
    }

    /**
     * <p>Return the {@link File} associated with a space template.</p>
     */
    private File file(String spaceKey) {
        final String home = this.bootstrapManager.getConfiguredConfluenceHome();
        final File templates = new File(home, "velocity");
        return spaceKey == null ? new File(templates, "autoexport.vm") :
               new File(templates, "autoexport." + spaceKey + ".vm");
    }

    /* ====================================================================== */
    /* PUBLICALLY ACCESSIBLE METHODS FOR TEMPLATE PARSING (VELOCITY)          */
    /* ====================================================================== */

    /**
     * <p>Parse and return the Velocity {@link Template} associated with the
     * specified space key.</p>
     */
    public Template getTemplate(String spaceKey)
    throws LocalizedException {
        String template = DEFAULT_TEMPLATE;
        if (this.hasCustomTemplate(spaceKey)) {
            if (spaceKey == null) template = "autoexport.vm";
            else template = "autoexport." + spaceKey + ".vm";
        } else if (this.hasCustomTemplate(null)) {
            template = "autoexport.vm";
        }

        final VelocityManager manager = VelocityManager.getInstance();
        final VelocityEngine engine = manager.getVelocityEngine();
        try {
            return engine.getTemplate(template, ENCODING);
        } catch (ResourceNotFoundException exception) {
            throw new LocalizedException(this, "template.notfound", template, exception);
        } catch (ParseErrorException exception) {
            throw new LocalizedException(this, "template.cantparse", template, exception);
        } catch (Exception exception) {
            throw new LocalizedException(this, "template.initerror", template, exception);
        }
    }

    /* ====================================================================== */
    /* PUBLICALLY ACCESSIBLE METHODS FOR TEMPLATE I/O                         */
    /* ====================================================================== */

    /**
     * <p>Read the default template shipped with this plugin.</p>
     * 
     * @return a <b>non-null</b> string with the template contents (escaped). 
     */
    public String readDefaultTemplate()
    throws LocalizedException {
        ClassLoader loader = this.getClass().getClassLoader();
        InputStream input = loader.getResourceAsStream(DEFAULT_TEMPLATE);
        if (input == null) {
            throw new LocalizedException(this, "reading.nodefault",
                                         "Can't find default template");
        } else try {
            return (this.read(input));
        } catch (IOException exception) {
            throw new LocalizedException(this, "reading.default", "Error read"
                                         + "ing default template", exception);
        }
    }

    /**
     * <p>Read the custom template associated with the space identified by the
     * specified key.</p>
     * 
     * @return the template contents (encoded) or <b>null</b> if no custom
     *         template was found. 
     */
    public String readCustomTemplate(String spaceKey)
    throws LocalizedException {

        final File file = this.file(spaceKey);
        if (! file.isFile()) {
            return spaceKey == null ? this.readDefaultTemplate() :
                                      this.readCustomTemplate(null);
        } else try {
            return read(new FileInputStream(file));
        } catch (IOException exception) {
            throw new LocalizedException(this, "reading.custom", spaceKey, exception);
        }
    }

    /**
     * <p>Check wether the space identified by the specifed key has a custom
     * template or not.</p>
     */
    public boolean hasCustomTemplate(String spaceKey) {
        return this.file(spaceKey).isFile();
    }

    /**
     * <p>Remove the custom template associated with the space identified by the
     * specified key.</p>
     * 
     * @return <b>true</b> if the custom template was removed successfully,
     *         <b>false</b> otherwise. 
     */
    public boolean removeCustomTemplate(String spaceKey)
    throws IOException {
        return this.file(spaceKey).delete();
    }

    /**
     * <p>Write the contents of the specified {@link String} as a custom
     * template for the space identified by the specified key.</p>
     *
     * @param spaceKey the key identifying the space.
     * @param template the contents of the template to write.
     */
    public void writeCustomTemplate(String spaceKey, String template)
    throws LocalizedException {
        /* Retrieve the file, and the velocity templates directory */
        final File file = this.file(spaceKey);
        final File directory = file.getParentFile();

        /* Attempt to write the template string down to a file */
        try {
            final File temp = File.createTempFile("tpl-", ".tmp", directory);
            final OutputStream output = new FileOutputStream(temp);
            output.write(template.getBytes(ENCODING));
            output.flush();
            output.close();

            if (! temp.renameTo(file)) {
                throw new LocalizedException(this, "writing.rename", new Object[] { temp, file });
            }
    
        } catch (IOException exception) {
            throw new LocalizedException(this, "writing.error", "Error writing "
                                         + "template contents", exception);
        }
        
        /* Wipe the cache within confluence of the template */
        final String templateName = file.getName();
        this.log.info(this.localizeMessage("template.flushing",
                                           new Object[] { templateName }));
        ConfluenceVelocityResourceCache.removeFromCaches(templateName);

        /* Ensure that the template can be parsed (and pre-cache it) */
        this.getTemplate(spaceKey);
    }

    /* ====================================================================== */
    /* BEAN SETTER METHODS FOR SPRING AUTO-WIRING                             */
    /* ====================================================================== */

    /**
     * <p>Setter for Spring's component wiring.</p>
     */
    public void setBootstrapManager(BootstrapManager bootstrapManager) {
        this.bootstrapManager = bootstrapManager;
    }
}
