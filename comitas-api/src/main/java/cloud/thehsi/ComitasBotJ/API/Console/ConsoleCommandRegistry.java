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
     * <p>
     * If the executor implements {@link ConsoleCommandTabCompleter}, it is used to complete the command's arguments.
     *
     * @param executor The ConsoleCommandExecutor for this commands
     * @param description The description of the command
     * @param aliases The aliases for this command
     */
    void register(@NotNull ConsoleCommandExecutor executor, @Nullable String description, @NotNull String... aliases);

    /**
     * Register a command
     *
     * @param executor The ConsoleCommandExecutor for this commands
     * @param tabCompleter The ConsoleCommandTabCompleter for this commands, null for no argument completion
     * @param description The description of the command
     * @param aliases The aliases for this command
     */
    void register(@NotNull ConsoleCommandExecutor executor, @Nullable ConsoleCommandTabCompleter tabCompleter, @Nullable String description, @NotNull String... aliases);

    /**
     * @return A list of all registered console commands
     */
    @NotNull
    @Unmodifiable
    List<ConsoleCommand> registeredCommands();

    /**
     * Represents a registered console command
     *
     * @param aliases                    The aliases for this command
     * @param plugin                     The plugin owning this command
     * @param description                The command description
     * @param consoleCommandExecutor     The console command executor belonging to this command
     * @param consoleCommandTabCompleter The console command tab completor belonging to this command
     */
    record ConsoleCommand(@NotNull List<String> aliases, @NotNull Plugin plugin, @NotNull String description,
                          @NotNull ConsoleCommandExecutor consoleCommandExecutor, @Nullable ConsoleCommandTabCompleter consoleCommandTabCompleter) {
    }
}
