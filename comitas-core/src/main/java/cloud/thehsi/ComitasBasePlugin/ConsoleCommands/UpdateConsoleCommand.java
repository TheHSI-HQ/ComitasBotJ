package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandTabCompleter;
import cloud.thehsi.ComitasBotJ.App.Updater;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpdateConsoleCommand extends ConsoleCommandExecutor implements ConsoleCommandTabCompleter {
    @NotNull
    final AtomicBoolean needsConfirmation = new AtomicBoolean(false);

    public UpdateConsoleCommand() {
        Comitas.getConsoleCommandRegistry().register(
                this,
                "Updates ComitasBotJ in place",
                "update"
        );
    }

    @Override
    public void execute(@NotNull String[] args, @NotNull String alias,
                        @NotNull ConsoleCommandRegistry.ConsoleCommand consoleCommand) {
        if (args.length == 0) {
            needsConfirmation.set(true);
            getConsoleLogger().info("To confirm the update, run update confirm within the 30 seconds");

            Comitas.getScheduler().runTaskLaterAsynchronously(Comitas.getPluginManager().getPlugin(), () -> needsConfirmation.set(false), 30, TimeUnit.SECONDS);
            return;
        } else if (Objects.equals(args[0], "confirm") && needsConfirmation.compareAndSet(true, false)) {
            // Consumed, so a second confirm doesn't start another update
            getConsoleLogger().info("Updating...");
            Updater.update();
            return;
        }

        getConsoleLogger().error("Usage: update [confirm]");
    }

    @Override
    public @NotNull String[] complete(@NotNull String[] args, @NotNull String alias,
                                      @NotNull ConsoleCommandRegistry.ConsoleCommand consoleCommand) {
        return args.length == 1 ? new String[]{"confirm"} : new String[0];
    }
}
