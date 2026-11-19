package cloud.thehsi.ComitasBotJ.API.Utility;

import org.jetbrains.annotations.NotNull;

/**
 * An Exception thrown incase command arguments could not be converted to their expected types
 */
public class CommandArgumentException extends RuntimeException {
    private final int argumentIndex;

    /**
     * @param message       The exception message
     * @param argumentIndex The index of the argument that caused the error
     */
    public CommandArgumentException(@NotNull String message, int argumentIndex) {
        super(message);
        this.argumentIndex = argumentIndex;
    }

    /**
     * @return The index of the argument that caused the error
     */
    public int getArgumentIndex() {
        return argumentIndex;
    }
}
