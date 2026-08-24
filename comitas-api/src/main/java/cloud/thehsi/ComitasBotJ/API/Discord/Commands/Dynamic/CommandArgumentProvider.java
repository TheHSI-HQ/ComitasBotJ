package cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandArgumentProvider {
    /**
     * Get the argument of a ran command (used in dynamic command registration)
     *
     * @param identifier Identifier of argument
     * @param type       Argument type
     * @return The command argument representation of the arguments value or null if not found
     */
    @Nullable <T> CommandArgument<T> get(@NotNull String identifier, @NotNull CommandArgumentType<T> type);

    <T> boolean has(@NotNull String identifier);

    <T> boolean has(@NotNull String identifier, @NotNull CommandArgumentType<T> type);
}
