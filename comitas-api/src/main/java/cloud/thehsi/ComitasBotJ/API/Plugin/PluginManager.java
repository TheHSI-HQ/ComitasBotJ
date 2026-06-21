package cloud.thehsi.ComitasBotJ.API.Plugin;

import cloud.thehsi.ComitasBotJ.PluginLoader.PluginLoaderManager;

import java.util.List;

public class PluginManager {
    private final PluginLoaderManager pluginLoaderManager;

    public PluginManager(PluginLoaderManager pluginLoaderManager) {
        this.pluginLoaderManager = pluginLoaderManager;
    }

    public Integer count() {
        return pluginLoaderManager.count();
    }

    public List<String> getPluginNames() {
        return pluginLoaderManager.pluginNameList();
    }

    public void reloadPlugins() {
        pluginLoaderManager.unloadPlugins();
        pluginLoaderManager.loadPlugins();
    }
}
