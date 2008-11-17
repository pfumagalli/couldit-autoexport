package it.could.confluence.autoexport;

import it.could.confluence.localization.LocalizedComponent;

import com.atlassian.config.ApplicationConfig;
import com.atlassian.config.bootstrap.AtlassianBootstrapManager;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.pages.thumbnail.ThumbnailManager;
import com.atlassian.renderer.WikiStyleRenderer;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.user.UserManager;

public class AutoExportManager extends LocalizedComponent {

    private ApplicationConfig applicationConfig = null;
    private UserManager userManager = null;
    private SpaceManager spaceManager = null;
    private AtlassianBootstrapManager bootstrapManager = null;
    private SettingsManager settingsManager = null;
    private PageManager pageManager = null;
    private ThumbnailManager thumbnailManager = null;
    private WikiStyleRenderer wikiStyleRenderer = null;
    private PluginAccessor pluginAccessor;
    private PermissionManager permissionManager = null;

    private boolean initialized;

    private ConfigurationManager configurationManager = null;
    private ExportManager exportManager = null;
    private LocationManager locationManager = null;
    private TemplatesManager templatesManager = null;

    /**
     * <p>Create a new {@link AutoExportManager} instance.</p>
     */
    public AutoExportManager() {
        this.log.info("Instance created");
    }
    
    private synchronized void initialize() {
        
        if (this.initialized) return;

        if (this.applicationConfig == null) throw new NullPointerException("Null ApplicationConfig instance");
        if (this.userManager == null) throw new NullPointerException("Null UserManager instance");
        if (this.spaceManager == null) throw new NullPointerException("Null SpaceManager instance");
        if (this.bootstrapManager == null) throw new NullPointerException("Null BootstrapManager instance");
        if (this.settingsManager == null) throw new NullPointerException("Null SettingsManager instance");
        if (this.pageManager == null) throw new NullPointerException("Null PageManager instance");
        if (this.thumbnailManager == null) throw new NullPointerException("Null ThumbnailManager instance");
        if (this.wikiStyleRenderer == null) throw new NullPointerException("Null WikiStyleRenderer instance");
        if (this.pluginAccessor == null) throw new NullPointerException("Null PluginAccessor instance");
        if (this.permissionManager == null) throw new NullPointerException("Null PermissionManager instance");

        this.configurationManager = new ConfigurationManager(this.userManager,
                                                             this.spaceManager,
                                                             this.bootstrapManager,
                                                             this.settingsManager,
                                                             this.applicationConfig);
        
        this.locationManager = new LocationManager(this.configurationManager,
                                                   this.permissionManager,
                                                   this.userManager);

        this.templatesManager = new TemplatesManager(this.configurationManager);

        this.exportManager = new ExportManager(this.templatesManager,
                                               this.locationManager,
                                               this.configurationManager,
                                               this.spaceManager,
                                               this.pageManager,
                                               this.thumbnailManager,
                                               this.wikiStyleRenderer,
                                               this.pluginAccessor);

        this.log.info("Instance initialized");
        this.initialized = true;
    }




    public ConfigurationManager getConfigurationManager() {
        this.initialize();
        return this.configurationManager;
    }

    public ExportManager getExportManager() {
        this.initialize();
        return this.exportManager;
    }

    public LocationManager getLocationManager() {
        this.initialize();
        return this.locationManager;
    }

    public TemplatesManager getTemplatesManager() {
        this.initialize();
        return this.templatesManager;
    }

    public void setApplicationConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public void setSpaceManager(SpaceManager spaceManager) {
        this.spaceManager = spaceManager;
    }

    public void setBootstrapManager(AtlassianBootstrapManager bootstrapManager) {
        this.bootstrapManager = bootstrapManager;
    }

    public void setSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    public void setPageManager(PageManager pageManager) {
        this.pageManager = pageManager;
    }

    public void setThumbnailManager(ThumbnailManager thumbnailManager) {
        this.thumbnailManager = thumbnailManager;
    }

    public void setWikiStyleRenderer(WikiStyleRenderer wikiStyleRenderer) {
        this.wikiStyleRenderer = wikiStyleRenderer;
    }

    public void setPluginAccessor(PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

}
