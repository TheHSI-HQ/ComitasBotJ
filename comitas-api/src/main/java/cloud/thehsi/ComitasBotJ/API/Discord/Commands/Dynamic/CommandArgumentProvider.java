package cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides parsed command arguments for use in dynamic commands
 */
public interface CommandArgumentProvider {
    /**
     * Get the argument of a ran command (used in dynamic command registration)
     *
     * @param identifier Identifier of argument
     * @param type       Argument type
     * @return The command argument representation of the arguments value or null if not found
     * @param <T>        The argument type
     */
    @Nullable <T> CommandArgument<T> get(@NotNull String identifier, @NotNull CommandArgumentType<T> type);

    /**
     * Check if an argument, identified by an identifier is present
     *
     * @param identifier The identifier to check
     * @return Is the argument present
     */
    boolean has(@NotNull String identifier);

    /**
     * Check if an argument, identified by an identifier is present and has a specified type
     *
     * @param identifier The identifier to check
     * @param type       The type to check
     * @param <T>        The raw type of the argument type
     * @return Is the argument present and of the specified type
     */
    <T> boolean has(@NotNull String identifier, @NotNull CommandArgumentType<T> type);
}
