package cloud.thehsi.ComitasBotJ.API.Plugin;

import cloud.thehsi.ComitasBotJ.API.Event.Listener;

import java.util.List;

@SuppressWarnings("unused")
public class PluginManager {
    private final InternalPluginManagerImpl impl;

    public PluginManager(InternalPluginManagerImpl impl) {
        this.impl = impl;
    }

    /**
     * Count the amount of loaded Plugins
     *
     * @return The amount of loaded Plugins
     */
    public Integer countPlugins() {
        return impl.countPlugins();
    }

    /**
     * Get the name of every loaded Plugin
     *
     * @return A List of every Loaded Plugin's Name
     */
    public List<Plugin.PluginMetadata> getAllPluginMetadata() {
        return impl.getAllPluginMetadata();
    }

    /**
     * Lookup any Plugin's Info, like its Name and Version
     *
     * @param plugin The plugin to be looked up
     * @return The Plugin's Metadata as a {@link cloud.thehsi.ComitasBotJ.API.Plugin.Plugin.PluginMetadata}
     */
    public Plugin.PluginMetadata lookupPlugin(Plugin plugin) {
        return impl.lookupPlugin(plugin);
    }

    /**
     * Reloads all plugins
     */
    public void reloadPlugins() {
        impl.reloadPlugins();
    }

    /**
     * Register a new Event Listener
     *
     * @param plugin   The plugin to which the event belongs
     * @param listener The event listener
     */
    public void registerEvents(Plugin plugin, Listener listener) {
        impl.registerEvents(plugin, listener);
    }
}
