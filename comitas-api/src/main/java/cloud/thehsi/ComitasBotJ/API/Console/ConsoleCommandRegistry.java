package cloud.thehsi.ComitasBotJ.API.Console;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public interface ConsoleCommandRegistry {
    /**
     * Register a command
     *
     * @param executor The CommandSupplier with the commands
     * @param description The description of the command
     * @param aliases The aliases for this command
     */
    void register(ConsoleCommandExecutor executor, @Nullable String description, String... aliases);

    List<ConsoleCommand> registeredCommands();

    record ConsoleCommand(String[] aliases, Plugin plugin, String description,
                          ConsoleCommandExecutor consoleCommandExecutor) {
    }
}
