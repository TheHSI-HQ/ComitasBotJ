package cloud.thehsi.ComitasBotJ.API.Plugin;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.UUID;

@SuppressWarnings("unused")
public abstract class Plugin {
    /**
     * Gets the plugin's {@link PersistentDataStorage}.
     * <p>
     * The {@link PersistentDataStorage} is used to store data across restarts.
     *
     * @return The {@link PersistentDataStorage} owned use by this {@link Plugin}
     */
    public static PersistentDataStorage getPersistentDataStorage() {
        return Comitas.getPluginManager().getPersistentDataStorage();
    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    public abstract void onEnable();

    public abstract void onDisable();

    public record PluginMetadata(String name, String version, String jarName, String targetAPI, UUID uuid,
                                 String consoleCommandPrefix) {
        public static PluginMetadata fromProperties(Properties props, String jarName) {
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
