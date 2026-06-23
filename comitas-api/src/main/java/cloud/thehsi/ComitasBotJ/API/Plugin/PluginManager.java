package cloud.thehsi.ComitasBotJ.API.Plugin;

import cloud.thehsi.ComitasBotJ.API.Event.Listener;

import java.util.List;

@SuppressWarnings("unused")
public class PluginManager {
    private final InternalPluginManagerImpl impl;

    public PluginManager(InternalPluginManagerImpl impl) {
        this.impl = impl;
    }

    public Integer countPlugins() {
        return impl.countPlugins();
    }

    public List<String> getPluginNames() {
        return impl.getPluginNames();
    }

    public Plugin.PluginMetadata lookupPlugin(Plugin plugin) {
        return impl.lookupPlugin(plugin);
    }

    public void reloadPlugins() {
        impl.reloadPlugins();
    }

    public void registerEvents(Plugin plugin, Listener listener) {
        impl.registerEvents(plugin, listener);
    }
}
