package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import cloud.thehsi.ComitasBotJ.Plugin.PluginLister;

public class PluginsConsoleCommand extends ConsoleCommand {
    public PluginsConsoleCommand() {
        Comitas.getConsoleCommandRegistry().register(
                this,
                "Lists all loaded Plugins",
                "plugins", "pl"
        );
    }

    @Override
    public void execute(String[] args) {
        PluginLister.listPlugins();
    }
}
