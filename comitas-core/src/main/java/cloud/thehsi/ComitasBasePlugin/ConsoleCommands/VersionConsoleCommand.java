package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import org.jetbrains.annotations.NotNull;

public class VersionConsoleCommand extends ConsoleCommand {
    public VersionConsoleCommand() {
        Comitas.getConsoleCommandRegistry().register(
                this,
                "Fetches the current version of ComitasBotJ",
                "version", "ver", "v", "about"
        );
    }

    @Override
    public void execute(@NotNull String[] args) {
        getConsoleLogger().info("Currently running:");
        getConsoleLogger().info("  ComitasBotJ {}v{}", "" + ConsoleColor.BLUE + ConsoleColor.BOLD, Comitas.getServerVersion());
    }
}
