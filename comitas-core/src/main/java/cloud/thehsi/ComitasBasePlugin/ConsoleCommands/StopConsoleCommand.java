package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

public class StopConsoleCommand extends ConsoleCommand {
    public StopConsoleCommand(Plugin plugin) {
        plugin.createCommandBuilder(this)
                .setDescription("Stops ComitasBotJ")
                .addCommand("stop")
                .addCommand("exit")
                .addCommand("quit")
                .addCommand("die")
                .register();
    }

    @Override
    public void execute(String[] args) {
        getConsoleLogger().info("Sending shutdown Signal");
        System.exit(0);
    }
}
