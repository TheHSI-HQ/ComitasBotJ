package cloud.thehsi.ComitasBotJ.API.Plugin;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataStorage;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

/**
 * Represents a loadable plugin
 */
@SuppressWarnings("unused")
public abstract class Plugin {
    /**
     * Gets the plugin's {@link PersistentDataStorage}.
     * <p>
     * The {@link PersistentDataStorage} is used to store data across restarts.
     *
     * @return The {@link PersistentDataStorage} owned use by this {@link Plugin}
     */
    @NotNull
    public static PersistentDataStorage getPersistentDataStorage() {
        return Comitas.getPluginManager().getPersistentDataStorage();
    }

    /**
     * Gets the plugin's private data folder ({@code plugin_data/<uuid>/} on the host).
     * <p>
     * Plugins run in a sandbox and can't access the host's file system directly,
     * the returned path is the root ({@code /}) of the plugin's own, isolated file system.
     * Use it with {@link java.nio.file.Files}, for example
     * {@code Files.writeString(getDataFolder().resolve("cache.txt"), "...")}.
     * <p>
     * Requires the {@link PluginPermission#FILE_STORAGE} permission.
     *
     * @return The root of the plugin's data folder
     * @throws SecurityException If the plugin wasn't granted {@link PluginPermission#FILE_STORAGE}
     */
    @NotNull
    public Path getDataFolder() {
        return Comitas.getPluginManager().getDataFolder();
    }

    /**
     * Gets a mount point, which the server owner made available to this plugin,
     * in the plugin's {@code .security.properties}.
     * <p>
     * Mount points are visible under {@code /mnt/<name>} in the plugin's file system.
     *
     * @param name The name of the mount point
     * @return The root of the mount point or null, if there is no such mount point
     */
    @Nullable
    public Path getMount(@NotNull String name) {
        return Comitas.getPluginManager().getMount(name);
    }

    /**
     * Checks if this plugin was granted a {@link PluginPermission}.
     *
     * @param permission The permission to check
     * @return True, if the permission was granted
     */
    public boolean hasPermission(@NotNull PluginPermission permission) {
        return Comitas.getPluginManager().hasPermission(permission);
    }

    /**
     * @return The plugins logger
     */
    @NotNull
    public Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    /**
     * The plugins on-load function, optional.
     * <p>
     * Called as soon as the plugin is loaded, before the bot is connected to Discord.
     * Discord dependant parts of the API (e.g. {@link Comitas#getBot()} or command registration) are not available yet.
     */
    public void onLoad() {
    }

    /**
     * The plugins on-enable function.
     * <p>
     * Called once the bot is connected to Discord, the whole API is available from here on.
     * This is the place to register commands and listeners.
     */
    public abstract void onEnable();

    /**
     * The plugins on-disable function.
     * <p>
     * Called when the plugin is unloaded, only if {@link #onEnable()} was called before.
     */
    public abstract void onDisable();

    /**
     * Stores plugin metadata
     *
     * @param name                 The plugin name
     * @param version              The plugin version
     * @param jarName              The plugin jar file name
     * @param targetAPI            The plugin target api
     * @param uuid                 The plugin uuid
     * @param consoleCommandPrefix The plugin console command prefix
     * @hidden
     */
    @ApiStatus.Internal
    public record PluginMetadata(String name, String version, String jarName, String targetAPI, UUID uuid,
                                 String consoleCommandPrefix) {
        /**
         * Create plugin metadata from Properties
         *
         * @param props The properties to convert from
         * @param jarName THe plugins jar file name
         * @return The converted plugin metadata
         * @hidden
         */
        @NotNull
        @ApiStatus.Internal
        public static PluginMetadata fromProperties(@NotNull Properties props, @NotNull String jarName) {
            if (!props.containsKey("name")) throw new RuntimeException("Plugin is missing name in plugin.properties");
            if (!props.containsKey("version"))
                throw new RuntimeException("Plugin is missing version in plugin.properties");
            if (!props.containsKey("api-target"))
                throw new RuntimeException("Plugin is missing api-target in plugin.properties");
            if (!props.containsKey("uuid"))
                throw new RuntimeException("Plugin is missing uuid in plugin.properties");

            try {
                return new PluginMetadata(
                        props.getProperty("name"),
                        props.getProperty("version"),
                        jarName,
                        props.getProperty("api-target"),
                        UUID.fromString(props.getProperty("uuid".toLowerCase())),
                        props.getProperty("command-prefix", props.getProperty("name"))
                );
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new RuntimeException("Plugin has an invalid uuid in plugin.properties");
            }
        }
    }
}
