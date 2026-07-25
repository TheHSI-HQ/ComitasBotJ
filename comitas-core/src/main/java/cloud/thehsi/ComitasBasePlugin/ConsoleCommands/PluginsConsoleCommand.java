package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.Plugin.PluginLister;

public class PluginsConsoleCommand extends ConsoleCommand {
    public PluginsConsoleCommand(Plugin plugin) {
        plugin.createCommandBuilder(this)
                .setDescription("Lists all loaded Plugins")
                .addCommand("plugins")
                .addCommand("pl")
                .register();
    }

    @Override
    public void execute(String[] args) {
        PluginLister.listPlugins();
    }
}
