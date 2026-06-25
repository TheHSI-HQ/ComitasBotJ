package cloud.thehsi.ComitasBotJ.API.Plugin;

import cloud.thehsi.ComitasBotJ.API.Event.Listener;

import java.util.List;

@SuppressWarnings("unused")
public interface PluginManager {
    /**
     * Count the amount of loaded Plugins
     *
     * @return The amount of loaded Plugins
     */
    Integer countPlugins();

    /**
     * Get the name of every loaded Plugin
     *
     * @return A List of every Loaded Plugin's Name
     */
    List<Plugin.PluginMetadata> getAllPluginMetadata();

    /**
     * Lookup any Plugin's Info, like its Name and Version
     *
     * @param plugin The plugin to be looked up
     * @return The Plugin's Metadata as a {@link cloud.thehsi.ComitasBotJ.API.Plugin.Plugin.PluginMetadata}
     */
    Plugin.PluginMetadata lookupPlugin(Plugin plugin);

    /**
     * Reloads all plugins
     */
    void reloadPlugins();

    /**
     * Register a new Event Listener
     *
     * @param plugin   The plugin to which the event belongs
     * @param listener The event listener
     */
    void registerEvents(Plugin plugin, Listener listener);
}
