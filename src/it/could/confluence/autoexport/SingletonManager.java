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

import bucket.container.ContainerManager;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginManager;

/**
 * <p>The {@link SingletonManager} class provides access to shared singleton
 * instances of objects within the AutoExport plugin.</p>
 * 
 * <p>This class is a work-around to a couple of problems I detected within
 * Confluence plugins, and their relation to the Spring framework.</p>
 * 
 * <p>The first (and most important one) is that the when the plugin is
 * reloaded Confluence throws an {@link IllegalStateException} mentioning "Can
 * not overwrite an existing bean definition: ...".</p>
 * 
 * <p>The second one (to investigate when the first issue is solved) is that
 * the plugin itself doesn't seem to be autowired, (we need to manually wire it
 * calling {@link ContainerManager#autowireComponent(Object)} in its own
 * constructor - not even in a constructor of a super-class, for example
 * {@link ComponentSupport}).</p>
 * 
 * <p>For further references, the bug to be tracked at Atlassian is
 * <a href="http://jira.atlassian.com/browse/CONF-5632">CONF-5632</a>.</p>
 */
public class SingletonManager {

    /* ====================================================================== */
    /* PLUGIN INFORMATION                                                     */
    /* ====================================================================== */

    /** <p>The name of the AutoExport plugin.</p> */
    public static final String PLUGIN_NAME;
    /** <p>The version number of the AutoExport plugin.</p> */
    public static final String PLUGIN_VERSION;
    /** <p>The URL of the AutoExport plugin.</p> */
    public static final String PLUGIN_URL;

    /* ====================================================================== */
    /* SINGLETON INSTANCES AND RELATED INITIALIZATION                         */
    /* ====================================================================== */

    private static final ConfigurationManager configurationManager;
    private static final TemplatesManager templatesManager;
    private static final LocationManager locationManager;
    private static final ExportManager exportManager;

    static {
        configurationManager = new ConfigurationManager();
        ContainerManager.autowireComponent(configurationManager);
        templatesManager = new TemplatesManager();
        ContainerManager.autowireComponent(templatesManager);
        locationManager = new LocationManager(configurationManager);
        ContainerManager.autowireComponent(locationManager);
        exportManager = new ExportManager(locationManager);
        ContainerManager.autowireComponent(exportManager);

        /* Get the plugin details from the Atlassian plugin descriptor */
        String name = null;
        String version = null;
        String url = null;
        try {
            final PluginManager pm =
                (PluginManager) ContainerManager.getComponent("pluginManager");
            final Plugin p = pm.getPlugin("confluence.extra.autoexport");
            final PluginInformation pi = p.getPluginInformation();
            name = p.getName();
            version = pi.getVersion();
            url = pi.getVendorUrl();
        } catch (NullPointerException exception) {
            name = "AutoExport Plugin";
            version = "Unknown - PluginManager Error";
            url = "http://could.it/autoexport/";
        } finally {
            PLUGIN_NAME = name;
            PLUGIN_VERSION = version;
            PLUGIN_URL = url;
        }
    }

    /* ====================================================================== */
    /* CONSTRUCTION                                                           */
    /* ====================================================================== */

    /** <p>Deny construction of instances.</p> */
    private SingletonManager() { }
    
    /* ====================================================================== */
    /* ACCESSOR METHODS TO SINGLETONS                                         */
    /* ====================================================================== */

    /** <p>Return the singleton {@link ConfigurationManager} instance.</p> */
    public static ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    /** <p>Return the singleton {@link TemplatesManager} instance.</p> */
    public static TemplatesManager getTemplatesManager() {
        return templatesManager;
    }

    /** <p>Return the singleton {@link LocationManager} instance.</p> */
    public static LocationManager getLocationManager() {
        return locationManager;
    }

    /** <p>Return the singleton {@link ExportManager} instance.</p> */
    public static ExportManager getExportManager() {
        return exportManager;
    }

}
