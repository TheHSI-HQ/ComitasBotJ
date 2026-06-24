package cloud.thehsi.ComitasBotJ.API.Plugin;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

@SuppressWarnings("unused")
public abstract class Plugin {
    public record PluginMetadata(String name, String version, String targetAPI, String consoleCommandPrefix) {
        public static PluginMetadata fromProperties(Properties props) {
            if (!props.containsKey("name")) throw new RuntimeException("Plugin is missing name in plugin.properties");
            if (!props.containsKey("version"))
                throw new RuntimeException("Plugin is missing version in plugin.properties");
            if (!props.containsKey("api-target"))
                throw new RuntimeException("Plugin is missing api-target in plugin.properties");

            return new PluginMetadata(
                    props.getProperty("name"),
                    props.getProperty("version"),
                    props.getProperty("api-target"),
                    props.getProperty("command-prefix", props.getProperty("name"))
            );
        }
    }

    public ConsoleCommandRegistry.CommandBuilder createCommandBuilder(ConsoleCommandExecutor executor) {
        return new ConsoleCommandRegistry.CommandBuilder(this, executor);
    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    public abstract void onEnable();

    public abstract void onDisable();
}
