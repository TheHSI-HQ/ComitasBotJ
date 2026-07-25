package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

import java.util.Objects;

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
        if (args.length == 1) {
            String verb = args[0].toLowerCase();
            if (verb.equals("hard")) {
                getConsoleLogger().info("Reloading...");
                Comitas.getPluginManager().reloadHard();
                return;
            }
            if (!verb.equals("soft")) {
                getConsoleLogger().error("Usage: reload [soft|hard]");
                return;
            }
        }

        getConsoleLogger().info("Reloading...");

        Comitas.getPluginManager().reloadSoft();
    }
}
