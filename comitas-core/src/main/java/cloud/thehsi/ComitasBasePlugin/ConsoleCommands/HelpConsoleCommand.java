package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

public class HelpConsoleCommand extends ConsoleCommand {
    public HelpConsoleCommand(Plugin plugin) {
        plugin.createCommandBuilder(this)
                .setDescription("Shows a Help Screen")
                .addCommand("help")
                .addCommand("?")
                .register();
    }

    @Override
    public void execute(String[] args) {
        getConsoleLogger().info("Help:");

        for (ConsoleCommandRegistry.Command cmd : Comitas.getConsoleCommandRegistry().registeredCommands()) {
            String joined = String.join(ConsoleColor.BRIGHT_BLACK + ", " + ConsoleColor.WHITE, cmd.aliases());
            if (cmd.description().isBlank())
                getConsoleLogger().info(" {}- {}{}", ConsoleColor.BRIGHT_BLACK, ConsoleColor.WHITE, joined);
            else
                getConsoleLogger().info(" {}- {}{}{}:{} {}", ConsoleColor.BRIGHT_BLACK, ConsoleColor.WHITE, joined, ConsoleColor.BRIGHT_BLACK, ConsoleColor.WHITE, cmd.description());
        }
    }
}
