package cloud.thehsi.ComitasBotJ.API.Console;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public interface ConsoleCommandRegistry {
    void register(ConsoleCommandRegistry.Command command);

    boolean runCommand(String command, String[] args);

    String[] validCommandList();

    List<ConsoleCommandRegistry.Command> registeredCommands();

    class CommandBuilder {
        final List<String> aliases;
        final String plugin_prefix;
        final Plugin plugin;
        String description = "";
        final ConsoleCommandExecutor consoleCommandExecutor;

        public CommandBuilder(Plugin plugin, ConsoleCommandExecutor consoleCommandExecutor) {
            this.plugin = plugin;
            Plugin.PluginMetadata metadata = Comitas.getPluginManager().lookupPlugin(plugin);

            if (metadata == null)
                throw new RuntimeException("CommandBuilder was initialized before the plugin was enabled");

            this.plugin_prefix = metadata.consoleCommandPrefix();
            this.consoleCommandExecutor = consoleCommandExecutor;
            this.aliases = new ArrayList<>();
        }

        /**
         * Register an Alias for this Command
         */
        public CommandBuilder addCommand(String command) {
            aliases.add(command);
            aliases.add(plugin_prefix + ":" + command);
            return this;
        }

        /**
         * Set the Description for this Command
         */
        public CommandBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Register this Command
         */
        public void register() {
            Comitas.getConsoleCommandRegistry().register(new ConsoleCommandRegistry.Command(aliases, plugin, description, consoleCommandExecutor));
        }
    }

    record Command(List<String> aliases, Plugin plugin, String description,
                          ConsoleCommandExecutor consoleCommandExecutor) {
    }
}
