package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import org.jetbrains.annotations.NotNull;

public class ReloadConsoleCommand extends ConsoleCommand {
    public ReloadConsoleCommand() {
        Comitas.getConsoleCommandRegistry().register(
                this,
                "Hot-Reload all Plugins",
                "reload", "rl"
        );
    }

    @Override
    public void execute(@NotNull String[] args) {
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
