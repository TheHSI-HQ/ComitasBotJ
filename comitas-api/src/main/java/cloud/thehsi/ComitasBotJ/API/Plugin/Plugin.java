package cloud.thehsi.ComitasBotJ.API.Plugin;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public abstract class Plugin {
    public record PluginMetadata(String name, String version, String consoleCommandPrefix) {
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
