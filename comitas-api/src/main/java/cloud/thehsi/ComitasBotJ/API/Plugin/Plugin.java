package cloud.thehsi.ComitasBotJ.API.Plugin;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataStorage;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @return The plugins logger
     */
    @NotNull
    public Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    /**
     * The plugins on-enable function
     */
    public abstract void onEnable();

    /**
     * The plugins on-disable function
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
