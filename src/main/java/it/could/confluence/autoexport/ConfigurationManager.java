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

import it.could.confluence.localization.LocalizedComponent;
import it.could.confluence.localization.LocalizedException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import com.atlassian.config.ApplicationConfig;
import com.atlassian.config.ConfigurationException;
import com.atlassian.config.ApplicationConfiguration;
import com.atlassian.config.bootstrap.AtlassianBootstrapManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.user.EntityException;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;

/**
 * <p>The {@link ConfigurationManager} wraps around Confluence's application
 * configuration and gives access to the configurations required by the
 * AutoExport plugin.</p>
 */
public class ConfigurationManager extends LocalizedComponent {

    /** <p>The prefix associated with our configuration properties.</p> */
    public static final String PREFIX = "autoexport.";
    /** <p>The name of the property identifying the status of the config.</p> */
    public static final String CONFIGURED = PREFIX + "configured";
    /** <p>The name of the property identifying the export encoding.</p> */
    public static final String ENCODING = PREFIX + "encoding";
    /** <p>The name of the property identifying the export root path.</p> */
    public static final String ROOT_PATH = PREFIX + "rootPath";
    /** <p>The name of the property identifying the export user name.</p> */
    public static final String USER_NAME = PREFIX + "userName";

    /** <p>The {@link UserManager} used to validate users.</p> */
    private final UserManager userManager;
    /** <p>The {@link SpaceManager} used to validate spaces.</p> */
    private final SpaceManager spaceManager;
    /** <p>The {@link AtlassianBootstrapManager} accessing core application properties.</p> */
    private final AtlassianBootstrapManager bootstrapManager;
    /** <p>The {@link SettingsManager} accessing core application settings.</p> */
    private final SettingsManager settingsManager;
    /** <p>The {@link ApplicationConfig} for configurations.</p> */
    private final ApplicationConfiguration applicationConfig;

    /** <p>The currently configured export encoding.</p> */
    private String encoding = null;
    /** <p>The currently configured root path.</p> */
    private String rootPath = null;
    /** <p>The currently configured user name.</p> */
    private String userName = null;

    /** <p>Create a new {@link ConfigurationManager} instance.</p> */
    public ConfigurationManager(UserManager userManager,
                         SpaceManager spaceManager,
                         AtlassianBootstrapManager bootstrapManager,
                         SettingsManager settingsManager,
                         ApplicationConfiguration applicationConfig) {

        this.userManager = userManager;
        this.spaceManager = spaceManager;
        this.bootstrapManager = bootstrapManager;
        this.settingsManager = settingsManager;
        this.applicationConfig = applicationConfig;
        this.reload();
        this.log.info("Instance created");
    }

    /* ====================================================================== */
    /* GENERIC CONFIGURATION METHODS                                          */
    /* ====================================================================== */

    /**
     * <p>Reload the configuration from Confluence's backend.</p>
     */
    private void reload() {
        this.encoding = (String) this.applicationConfig.getProperty(ENCODING);
        this.rootPath = (String) this.applicationConfig.getProperty(ROOT_PATH);
        this.userName = (String) this.applicationConfig.getProperty(USER_NAME);
    }

    /**
     * <p>Save all modifications made to this instance onto the configuration
     * properties file.</p>
     */
    public void save()
    throws LocalizedException {

        if (this.encoding == null) this.applicationConfig.removeProperty(ENCODING);
        else this.applicationConfig.setProperty(ENCODING, this.encoding);

        if (this.rootPath == null) this.applicationConfig.removeProperty(ROOT_PATH);
        else this.applicationConfig.setProperty(ROOT_PATH, this.rootPath);

        if (this.userName == null) this.applicationConfig.removeProperty(USER_NAME);
        else this.applicationConfig.setProperty(USER_NAME, this.userName);
        
        this.applicationConfig.setProperty(CONFIGURED, true);

        try {
            this.applicationConfig.save();
        } catch (ConfigurationException exception) {
            throw new LocalizedException(this, "save.error", "Exception saving "
                                         + "plugin configuration", exception);
        } finally {
            this.reload();
        }
    }

    /**
     * <p>Wipe out the entire configuration and remove the properties file.</p>
     */
    public void delete()
    throws LocalizedException {
        final Map map = this.applicationConfig.getPropertiesWithPrefix(PREFIX);
        for (Iterator iterator = map.keySet().iterator(); iterator.hasNext(); ) {
            this.applicationConfig.removeProperty(iterator.next());
        }
        try {
            this.applicationConfig.save();
        } catch (ConfigurationException exception) {
            throw new LocalizedException(this, "delete.error", "Exception deleting"
                                         + " plugin configuration", exception);
        } finally {
            this.reload();
        }
    }

    /**
     * <p>Checks whether the AutoExport plugin has been properly configured.</p>
     * 
     * @return <b>true</b> if the configuration exists and it is valid,
     *         <b>false</b> if the configuration does not exist.
     * @throws LocalizedException if the current configuration is not valid.
     */
    public boolean isConfigured()
    throws LocalizedException {
        if (this.applicationConfig.getBooleanProperty(CONFIGURED)) {
            this.validateEncoding(this.getEncoding());
            this.validateRootPath(this.getRootPath());
            this.validateUserName(this.getUserName());
            return true;
        } else {
            return false;
        }
    }

    /* ====================================================================== */
    /* SETTER AND GETTER METHODS FOR CONFIGURATION VALUES                     */
    /* ====================================================================== */

    /**
     * <p>Return the configured export encoding.</p> 
     */
    public String getEncoding() {
        return this.encoding != null ? this.encoding :
               new OutputStreamWriter(new ByteArrayOutputStream()).getEncoding();
    }

    /**
     * <p>Set the export encoding in the configuration.</p> 
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * <p>Return the configured export root path.</p> 
     */
    public String getRootPath() {
        return this.rootPath;
    }

    /**
     * <p>Set the export root path in the configuration.</p> 
     */
    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    /**
     * <p>Return the configured user that will access the content to be
     * exported.</p> 
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * <p>Set the user that will access the content to be exported in the
     * configuration.</p> 
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * <p>Return the extension used for the auto-exported files.</p>
     * 
     * <p>TODO: Make this a configurable item.</p> 
     */
    public String getExtension() {
        return ".html";
    }

    /**
     * <p>Return the base URL of Confluence's deployment.</p>
     * 
     * <p>TODO: Make this a configurable item.</p> 
     */
    public String getConfluenceUrl() {
        return this.settingsManager.getGlobalSettings().getBaseUrl();
    }

    /**
     * <p>Return the home directory of Confluence's deployment.</p>
     * 
     * <p>TODO: Make this a configurable item.</p> 
     */
    public String getConfluenceHome() {
        return this.bootstrapManager.getApplicationHome();
    }

    /* ====================================================================== */
    /* CUSTOM VALIDATORS FOR CONFIGURATION VALUES                             */
    /* ====================================================================== */

    /**
     * <p>Validate the specified {@link String} as the export encoding.</p> 
     */
    public String validateEncoding(String encoding)
    throws LocalizedException {
        try {
            "test".getBytes(encoding);
            return encoding;
        } catch (UnsupportedEncodingException exception) {
            throw new LocalizedException(this, "encoding.invalid", encoding, exception);
        }
    }

    /**
     * <p>Validate the specified {@link String} as the export root path.</p> 
     */
    public String validateRootPath(String rootPath)
    throws LocalizedException {

        /* Verify that the root path specified is not empty or null */
        if ("".equals(rootPath)) rootPath = null;
        if (rootPath == null) {
            throw new LocalizedException(this, "rootPath.no",
                                         "No root path specified");
        }

        /* Convert the root path into a file relative to Confluence's home */
        File rootFile = new File(rootPath);
        if (! rootFile.isAbsolute()) {
            String home = this.bootstrapManager.getConfiguredApplicationHome();
            rootFile = new File(home, rootPath);
        }

        /* Canonicalize the root path */
        try {
            rootFile = rootFile.getCanonicalFile();
        } catch (IOException exception) {
            throw new LocalizedException(this, "rootPath.canon", rootFile, exception);
        }

        /* Verify that the root path is a valid writable directory */
        if (! rootFile.exists())
            throw new LocalizedException(this, "rootPath.notfound", rootFile);
        if (! rootFile.isDirectory())
            throw new LocalizedException(this, "rootPath.invalid", rootFile);
        if (! rootFile.canWrite())
            throw new LocalizedException(this, "rootPath.readonly", rootFile);

        /* Return the canonicalized root path */
        return rootFile.toString();
    }

    /**
     * <p>Validate the specified {@link String} as the user that will access the
     * content to be exported.</p> 
     */
    public String validateUserName(String userName)
    throws LocalizedException {

        /* If the user name is null (or empty) we mean the anonymous user */
        if ("".equals(userName)) userName = null;
        if (userName == null) return null;

        /* Look up the user in the user manager and validate it */
        try {
            User user = this.userManager.getUser(userName);
            if (user != null) return user.getName();
            throw new LocalizedException(this, "userName.unknown", userName);
        } catch (EntityException exception) {
            throw new LocalizedException(this, "userName.error", userName, exception);
        }
    }

    /**
     * <p>Validate the specified {@link String} as the space representing the
     * home page of the exported site.</p> 
     */
    public String validateHomeSpace(String homeSpace)
    throws LocalizedException {

        /* The home space can be null when there is no home space */
        if ("".equals(homeSpace)) homeSpace = null;
        if (homeSpace == null) return null;

        /* Validate the space by trying to instantiate it */
        Space space = this.spaceManager.getSpace(homeSpace);
        if (space != null) return space.getKey();
        throw new LocalizedException(this, "homeSpace.unknown", homeSpace);
    }
}
