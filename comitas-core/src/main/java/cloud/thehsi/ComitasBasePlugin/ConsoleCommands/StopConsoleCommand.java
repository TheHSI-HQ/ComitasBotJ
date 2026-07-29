package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;

public class StopConsoleCommand extends ConsoleCommand {
    public StopConsoleCommand() {
        Comitas.getConsoleCommandRegistry().register(
                this,
                "Stops ComitasBotJ",
                "stop", "exit", "quit", "die"
        );
    }

    @Override
    public void execute(String[] args) {
        getConsoleLogger().info("Sending shutdown Signal");
        System.exit(0);
    }
}
