package cloud.thehsi.ComitasBotJ.API.Plugin;

import cloud.thehsi.ComitasBotJ.API.Event.Listener;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the plugin manager, which handles most actions and api calls
 * between the plugin and comitas, not connected to the bot itself
 */
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
     * Gets the current plugin's data folder, see {@link Plugin#getDataFolder()}.
     *
     * @return The root of the current plugin's data folder
     */
    @NotNull
    Path getDataFolder();

    /**
     * Gets a mount point of the current plugin, see {@link Plugin#getMount(String)}.
     *
     * @param name The name of the mount point
     * @return The root of the mount point or null, if there is no such mount point
     */
    @Nullable
    Path getMount(@NotNull String name);

    /**
     * Lists the mount points of the current plugin, see {@link Plugin#getMounts()}.
     *
     * @return The mount points, sorted by name
     */
    @NotNull
    @Unmodifiable
    List<PluginMount> getMounts();

    /**
     * Asks the server owner for a new mount point, see {@link Plugin#requestMount(String, String, boolean)}.
     *
     * @param name     The name of the mount point, only {@code A-Z a-z 0-9 _ -}
     * @param reason   Why the plugin needs the directory, shown to the server owner
     * @param readOnly If the plugin only needs to read from the directory
     * @return A future completing with the root of the mount point, or null if it was denied
     */
    @NotNull
    CompletableFuture<@Nullable Path> requestMount(@NotNull String name, @NotNull String reason, boolean readOnly);

    /**
     * Checks if the current plugin was granted a {@link PluginPermission}.
     *
     * @param permission The permission to check
     * @return True, if the permission was granted
     */
    boolean hasPermission(@NotNull PluginPermission permission);

    /**
     * Lookup any Plugin's Info, like its Name and Version
     *
     * @param plugin The plugin to be looked up
     * @return The Plugin's Metadata as a {@link cloud.thehsi.ComitasBotJ.API.Plugin.Plugin.PluginMetadata}
     */
    @Nullable
    Plugin.PluginMetadata lookupPlugin(@NotNull Plugin plugin);

    /**
     * Reloads all plugins, without reconnecting the bot.
     * <p>
     * Plugins get {@link Plugin#onLoad()} and {@link Plugin#onEnable()} called right after each other.
     */
    void reloadSoft();

    /**
     * Reloads all plugins, the config and the bot itself.
     * <p>
     * Plugins get {@link Plugin#onLoad()} called right away and {@link Plugin#onEnable()} once the bot reconnected.
     */
    void reloadHard();

    /**
     * Register a new Event Listener
     *
     * @param plugin   The plugin to which the event belongs
     * @param listener The event listener
     */
    void registerEvents(@NotNull Plugin plugin, @NotNull Listener listener);

    /**
     * Broadcasts (sends) a package to ALL plugins,
     * packages are mutible and plugins can modify
     * them.
     * <p>
     * These can be listened for {@link cloud.thehsi.ComitasBotJ.API.Event.Events.PackageSentEvent}
     *
     * @param pack The package to broadcast
     * @param <T>  The class of the package
     */
    <T extends Package> void sendPackage(@NotNull T pack);
}
