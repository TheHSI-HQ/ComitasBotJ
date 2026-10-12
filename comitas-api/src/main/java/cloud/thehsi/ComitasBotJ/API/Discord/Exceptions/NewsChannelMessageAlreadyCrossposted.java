package cloud.thehsi.ComitasBotJ.API.Discord.Exceptions;

import org.jetbrains.annotations.Nullable;

/**
 * An exception thrown, if a message has already been crossposted
 */
public class NewsChannelMessageAlreadyCrossposted extends RuntimeException {
    /**
     * @param message The exception message
     */
    public NewsChannelMessageAlreadyCrossposted(@Nullable String message) {
        super(message);
    }
}
