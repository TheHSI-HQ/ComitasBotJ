package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import cloud.thehsi.ComitasBotJ.Updater;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpdateConsoleCommand extends ConsoleCommand {
    final AtomicBoolean needsConfirmation = new AtomicBoolean(false);

    public UpdateConsoleCommand() {
        Comitas.getConsoleCommandRegistry().register(
                this,
                "Updates ComitasBotJ in place",
                "update"
        );
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            needsConfirmation.set(true);
            getConsoleLogger().info("To confirm the update, run update confirm within the 30 seconds");

            Comitas.getScheduler().runTaskLaterAsynchronously(Comitas.getPluginManager().getPlugin(), () -> needsConfirmation.set(false), 30000);
            return;
        } else if (Objects.equals(args[0], "confirm") && needsConfirmation.get()) {
            getConsoleLogger().info("Updating...");
            Updater.update();
            return;
        }

        getConsoleLogger().error("Usage: update [confirm]");
    }
}
