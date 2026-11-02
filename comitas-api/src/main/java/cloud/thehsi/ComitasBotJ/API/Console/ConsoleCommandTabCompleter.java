package cloud.thehsi.ComitasBotJ.API.Console;

import org.jetbrains.annotations.NotNull;

/**
 * Suggests completions for the arguments of a console command, when TAB is pressed in the console.
 * <p>
 * Pass it to {@link ConsoleCommandRegistry#register(ConsoleCommandExecutor, ConsoleCommandTabCompleter, String, String...)},
 * or implement it in your {@link ConsoleCommandExecutor}, it is then used automatically.
 * The command name itself is completed by Comitas.
 */
@FunctionalInterface
public interface ConsoleCommandTabCompleter {
    /**
     * Suggests completions for the argument, which is currently typed.
     * <p>
     * The last element of {@code args} is the argument being completed, up to the cursor. It is empty, if a new
     * argument was just started. Suggestions don't have to be filtered by what was typed already,
     * the console only shows the matching ones.
     *
     * @param args           The arguments typed so far, without the command itself
     * @param alias          The command alias used
     * @param consoleCommand The console command, which is completed
     * @return The suggestions for the last argument, may be empty
     */
    @NotNull
    String[] complete(@NotNull String[] args, @NotNull String alias, @NotNull ConsoleCommandRegistry.ConsoleCommand consoleCommand);
}
