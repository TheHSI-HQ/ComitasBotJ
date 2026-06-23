package cloud.thehsi.ComitasBotJ.API.Console;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ConsoleCommandRegistry {
    private final InternalConsoleCommandRegistryImpl impl;

    public ConsoleCommandRegistry(InternalConsoleCommandRegistryImpl impl) {
        this.impl = impl;
    }

    public static CommandBuilder builder(Plugin plugin, ConsoleCommandExecutor consoleCommandExecutor) {
        return new CommandBuilder(plugin, consoleCommandExecutor);
    }

    public void register(ConsoleCommandRegistry.Command command) {
        impl.register(command);
    }

    public boolean runCommand(String command, String[] args) {
        return impl.runCommand(command, args);
    }

    public String[] validCommandList() {
        return impl.validCommandList();
    }

    public List<ConsoleCommandRegistry.Command> registeredCommands() {
        return impl.registeredCommands();
    }

    public static class CommandBuilder {
        List<String> aliases;
        String plugin_prefix;
        Plugin plugin;
        String description = "";
        ConsoleCommandExecutor consoleCommandExecutor;

        public CommandBuilder(Plugin plugin, ConsoleCommandExecutor consoleCommandExecutor) {
            this.plugin = plugin;
            Plugin.PluginMetadata metadata = Comitas.getPluginManager().lookupPlugin(plugin);

            if (metadata == null)
                throw new RuntimeException("CommandBuilder was initialized before the plugin was enabled");

            this.plugin_prefix = metadata.consoleCommandPrefix();
            this.consoleCommandExecutor = consoleCommandExecutor;
            this.aliases = new ArrayList<>();
        }

        public CommandBuilder addCommand(String command) {
            aliases.add(command);
            aliases.add(plugin_prefix + ":" + command);
            return this;
        }

        public CommandBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public void register() {
            Comitas.getConsoleCommandRegistry().register(new Command(aliases, plugin, description, consoleCommandExecutor));
        }
    }

    public record Command(List<String> aliases, Plugin plugin, String description,
                          ConsoleCommandExecutor consoleCommandExecutor) {
    }
}
