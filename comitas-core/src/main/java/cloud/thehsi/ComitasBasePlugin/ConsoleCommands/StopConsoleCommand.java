package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import org.jetbrains.annotations.NotNull;

public class StopConsoleCommand extends ConsoleCommandExecutor {
    public StopConsoleCommand() {
        Comitas.getConsoleCommandRegistry().register(
                this,
                "Stops ComitasBotJ",
                "stop", "exit", "quit", "die"
        );
    }

    @Override
    public void execute(@NotNull String[] args, @NotNull String alias,
                        @NotNull ConsoleCommandRegistry.ConsoleCommand consoleCommand) {
        getConsoleLogger().info("Sending shutdown Signal");
        System.exit(0);
    }
}
