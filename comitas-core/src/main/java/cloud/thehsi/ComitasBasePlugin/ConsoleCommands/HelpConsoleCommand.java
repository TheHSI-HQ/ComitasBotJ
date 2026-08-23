package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import org.jetbrains.annotations.NotNull;

public class HelpConsoleCommand extends ConsoleCommand {
    public HelpConsoleCommand() {
        Comitas.getConsoleCommandRegistry().register(
                this,
                "Shows a Help Screen",
                "help", "?"
        );
    }

    @Override
    public void execute(@NotNull String[] args) {
        getConsoleLogger().info("Help:");

        for (ConsoleCommandRegistry.ConsoleCommand cmd : Comitas.getConsoleCommandRegistry().registeredCommands()) {
            String joined = String.join(ConsoleColor.BRIGHT_BLACK + ", " + ConsoleColor.WHITE, cmd.aliases());
            if (cmd.description().isBlank())
                getConsoleLogger().info(" {}- {}{}", ConsoleColor.BRIGHT_BLACK, ConsoleColor.WHITE, joined);
            else
                getConsoleLogger().info(" {}- {}{}{}:{} {}", ConsoleColor.BRIGHT_BLACK, ConsoleColor.WHITE, joined, ConsoleColor.BRIGHT_BLACK, ConsoleColor.WHITE, cmd.description());
        }
    }
}
