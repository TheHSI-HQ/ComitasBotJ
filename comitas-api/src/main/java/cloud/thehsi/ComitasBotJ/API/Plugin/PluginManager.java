package cloud.thehsi.ComitasBotJ.API.Plugin;

import cloud.thehsi.ComitasBotJ.API.Event.Listener;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@SuppressWarnings("unused")
public interface PluginManager {
    /**
     * Count the amount of loaded Plugins
     *
     * @return The amount of loaded Plugins
     */
    int countPlugins();

    /**
     * Get the name of every loaded Plugin
     *
     * @return A List of every Loaded Plugin's Name
     */
    @NotNull
    @Unmodifiable
    List<Plugin.PluginMetadata> getAllPluginMetadata();

    /**
     * Gets the current {@link Plugin}.
     *
     * @return The {@link Plugin} who called this function
     */
    @NotNull
    Plugin getPlugin();

    /**
     * Gets the plugin's {@link PersistentDataStorage}.
     * <p>
     * The {@link PersistentDataStorage} is used to store data across restarts.
     *
     * @return The {@link PersistentDataStorage} owned use by the current {@link Plugin}
     */
    @NotNull
    PersistentDataStorage getPersistentDataStorage();

    /**
     * Lookup any Plugin's Info, like its Name and Version
     *
     * @param plugin The plugin to be looked up
     * @return The Plugin's Metadata as a {@link cloud.thehsi.ComitasBotJ.API.Plugin.Plugin.PluginMetadata}
     */
    @Nullable
    Plugin.PluginMetadata lookupPlugin(@NotNull Plugin plugin);

    /**
     * Reloads all plugins and sends a fake bot ready even
     */
    void reloadSoft();

    /**
     * Reloads all plugins, the config and the bot itself.
     */
    void reloadHard();

    /**
     * Register a new Event Listener
     *
     * @param plugin   The plugin to which the event belongs
     * @param listener The event listener
     */
    void registerEvents(@NotNull Plugin plugin, @NotNull Listener listener);
}
