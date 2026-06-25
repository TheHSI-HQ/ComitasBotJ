package cloud.thehsi.ComitasBotJ.Plugin;

import cloud.thehsi.ComitasBotJ.API.Event.Listener;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
import cloud.thehsi.ComitasBotJ.Event.EventManager;
import cloud.thehsi.ComitasBotJ.Scheduler.InternalScheduler;

import java.util.List;

public class InternalPluginManager implements PluginManager {
    private final PluginLoaderManager pluginLoaderManager;
    private final EventManager eventManager;
    private final InternalScheduler scheduler;

    public InternalPluginManager(PluginLoaderManager pluginLoaderManager, EventManager eventManager, InternalScheduler scheduler) {
        this.pluginLoaderManager = pluginLoaderManager;
        this.eventManager = eventManager;
        this.scheduler = scheduler;
    }

    @Override
    public Integer countPlugins() {
        return pluginLoaderManager.count();
    }

    @Override
    public List<Plugin.PluginMetadata> getAllPluginMetadata() {
        return pluginLoaderManager.pluginMetadataList();
    }

    @Override
    public Plugin.PluginMetadata lookupPlugin(Plugin plugin) {
        return pluginLoaderManager.lookupPlugin(plugin);
    }

    @Override
    public void reloadPlugins() {
        pluginLoaderManager.unloadPlugins();
        scheduler.cancelAll();
        eventManager.clearEvents();
        pluginLoaderManager.loadPlugins();
    }

    @Override
    public void registerEvents(Plugin plugin, Listener listener) {
        eventManager.registerListener(plugin, listener);
    }
}

