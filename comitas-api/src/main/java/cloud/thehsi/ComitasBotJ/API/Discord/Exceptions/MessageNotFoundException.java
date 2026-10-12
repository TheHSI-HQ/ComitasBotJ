package cloud.thehsi.ComitasBotJ.API.Discord.Exceptions;

import org.jetbrains.annotations.Nullable;

/**
 * An exception thrown, if a message could not be found
 */
public class MessageNotFoundException extends RuntimeException {
    /**
     * @param message The exception message
     */
    public MessageNotFoundException(@Nullable String message) {
        super(message);
    }
}
