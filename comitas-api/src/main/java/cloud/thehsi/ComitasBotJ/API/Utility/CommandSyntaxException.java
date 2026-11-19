package cloud.thehsi.ComitasBotJ.API.Utility;

import org.jetbrains.annotations.NotNull;

/**
 * An Exception thrown incase a command could not be parsed due to invalid syntax
 */
public class CommandSyntaxException extends RuntimeException {
    private final int position;

    /**
     * @param message  The exception message
     * @param position The index in the raw command at which the error occurred
     */
    public CommandSyntaxException(@NotNull String message, int position) {
        super(message + " at position " + position);
        this.position = position;
    }

    /**
     * @return The index in the raw command at which the error occurred
     */
    public int getPosition() {
        return position;
    }
}
