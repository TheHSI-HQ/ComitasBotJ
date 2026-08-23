package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import cloud.thehsi.ComitasBotJ.Plugin.PluginLister;
import org.jetbrains.annotations.NotNull;

public class PluginsConsoleCommand extends ConsoleCommand {
    public PluginsConsoleCommand() {
        Comitas.getConsoleCommandRegistry().register(
                this,
                "Lists all loaded Plugins",
                "plugins", "pl"
        );
    }

    @Override
    public void execute(@NotNull String[] args) {
        PluginLister.listPlugins();
    }
}
