package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.Main;

public class VersionConsoleCommand extends ConsoleCommand {
    public VersionConsoleCommand(Plugin plugin) {
        plugin.createCommandBuilder(this)
                .setDescription("Fetches the current Version of ComitasBotJ")
                .addCommand("version")
                .addCommand("ver")
                .addCommand("v")
                .addCommand("about")
                .register();
    }

    @Override
    public void execute(String[] args) {
        getConsoleLogger().info("Currently running ComitasBotJ {}", Main.VERSION);
    }
}
