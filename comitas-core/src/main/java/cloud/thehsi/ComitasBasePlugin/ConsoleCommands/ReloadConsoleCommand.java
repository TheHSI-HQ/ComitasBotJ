package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandTabCompleter;
import org.jetbrains.annotations.NotNull;

public class ReloadConsoleCommand extends ConsoleCommandExecutor implements ConsoleCommandTabCompleter {
    public ReloadConsoleCommand() {
        Comitas.getConsoleCommandRegistry().register(
                this,
                "Hot-Reload all Plugins",
                "reload", "rl"
        );
    }

    @Override
    public void execute(@NotNull String[] args, @NotNull String alias,
                        @NotNull ConsoleCommandRegistry.ConsoleCommand consoleCommand) {
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

    @Override
    public @NotNull String[] complete(@NotNull String[] args, @NotNull String alias,
                                      @NotNull ConsoleCommandRegistry.ConsoleCommand consoleCommand) {
        return args.length == 1 ? new String[]{"soft", "hard"} : new String[0];
    }
}
