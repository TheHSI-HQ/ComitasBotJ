package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

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
        getConsoleLogger().info("Currently running:");
        getConsoleLogger().info("  ComitasBotJ {}v{}", "" + ConsoleColor.BLUE + ConsoleColor.BOLD, Comitas.getServerVersion());
        getConsoleLogger().info("  Comitas API {}v{}", "" + ConsoleColor.BLUE + ConsoleColor.BOLD, Comitas.getAPIVersion());
    }
}
