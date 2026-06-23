package cloud.thehsi.ComitasBotJ.Plugin;

import cloud.thehsi.ComitasBotJ.API.Event.Listener;
import cloud.thehsi.ComitasBotJ.API.Plugin.InternalPluginManagerImpl;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.Event.EventManager;

import java.util.List;

public class InternalPluginManager implements InternalPluginManagerImpl {
    private final PluginLoaderManager pluginLoaderManager;
    private final EventManager eventManager;

    public InternalPluginManager(PluginLoaderManager pluginLoaderManager, EventManager eventManager) {
        this.pluginLoaderManager = pluginLoaderManager;
        this.eventManager = eventManager;
    }

    @Override
    public Integer countPlugins() {
        return pluginLoaderManager.count();
    }

    @Override
    public List<String> getPluginNames() {
        return pluginLoaderManager.pluginNameList();
    }

    @Override
    public Plugin.PluginMetadata lookupPlugin(Plugin plugin) {
        return pluginLoaderManager.lookupPlugin(plugin);
    }

    @Override
    public void reloadPlugins() {
        pluginLoaderManager.unloadPlugins();
        eventManager.clearEvents();
        pluginLoaderManager.loadPlugins();
    }

    @Override
    public void registerEvents(Plugin plugin, Listener listener) {
        eventManager.registerListener(plugin, listener);
    }
}

