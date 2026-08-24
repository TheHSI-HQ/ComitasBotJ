package cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandArgument<T> {
    /**
     * The raw value of the argument,
     * only null if original argument was marked as optional,
     * and nothing was provided
     *
     * @return The raw value
     */
    @Nullable Object getValue();

    /**
     * Is this argument optional
     *
     * @return Is optional argument
     */
    boolean isOptional();

    /**
     * Get the CommandArgumentType
     *
     * @return The CommandArgumentType
     */
    @NotNull CommandArgumentType<T> getType();
}
