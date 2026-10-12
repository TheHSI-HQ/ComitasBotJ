package cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum;

import org.jetbrains.annotations.Nullable;

/**
 * An exception thrown incase a {@link ForumTag} isn't unique
 */
public class ForumTagNameNotUniqueException extends RuntimeException {
    /**
     * @param message The exception message
     */
    public ForumTagNameNotUniqueException(@Nullable String message) {
        super(message);
    }
}
