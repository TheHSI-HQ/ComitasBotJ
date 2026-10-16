package cloud.thehsi.ComitasBotJ.API.Console;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * The registry for console commands,
 * used to register console commands
 */
@SuppressWarnings("unused")
public interface ConsoleCommandRegistry {
    /**
     * Register a command
     *
     * @param executor The CommandSupplier with the commands
     * @param description The description of the command
     * @param aliases The aliases for this command
     */
    void register(@NotNull ConsoleCommandExecutor executor, @Nullable String description, @NotNull String... aliases);

    /**
     * @return A list of all registered console commands
     */
    @NotNull
    @Unmodifiable
    List<ConsoleCommand> registeredCommands();

    /**
     * Represents a registered console command
     *
     * @param aliases                The aliases for this command
     * @param plugin                 The plugin owning this command0
     * @param description            The command description
     * @param consoleCommandExecutor The console command executor bleonging to this command
     */
    record ConsoleCommand(@NotNull List<String> aliases, @NotNull Plugin plugin, @NotNull String description,
                          @NotNull ConsoleCommandExecutor consoleCommandExecutor) {
    }
}
