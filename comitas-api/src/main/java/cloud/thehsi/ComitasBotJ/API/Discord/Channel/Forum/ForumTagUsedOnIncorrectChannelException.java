package cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum;

import org.jetbrains.annotations.Nullable;

/**
 * An exception thrown incase a {@link ForumTag} is used on the wrong channel.
 */
public class ForumTagUsedOnIncorrectChannelException extends RuntimeException {
    /**
     * @param message The exception message
     */
    public ForumTagUsedOnIncorrectChannelException(@Nullable String message) {
        super(message);
    }
}
