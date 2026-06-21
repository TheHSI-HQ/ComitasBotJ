package cloud.thehsi.ComitasBotJ.API.Plugin;

import cloud.thehsi.ComitasBotJ.PluginLoader.PluginLoaderManager;

public class PluginManager {
    private final PluginLoaderManager pluginLoaderManager;

    public PluginManager(PluginLoaderManager pluginLoaderManager) {
        this.pluginLoaderManager = pluginLoaderManager;
    }

    public Integer count() {
        return pluginLoaderManager.count();
    }

    public void reloadPlugins() {
        pluginLoaderManager.loadPlugins();
    }
}
