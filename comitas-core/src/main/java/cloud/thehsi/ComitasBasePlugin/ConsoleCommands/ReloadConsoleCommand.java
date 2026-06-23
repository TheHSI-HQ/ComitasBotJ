package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

public class ReloadConsoleCommand extends ConsoleCommand {
    public ReloadConsoleCommand(Plugin plugin) {
        plugin.createCommandBuilder(this)
                .setDescription("Hot-Reload all Plugins")
                .addCommand("reload")
                .addCommand("rl")
                .register();
    }

    @Override
    public void execute(String[] args) {
        getConsoleLogger().info("Reloading...");

        Comitas.getPluginManager().reloadPlugins();
    }
}
