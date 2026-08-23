package cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum;

import org.jetbrains.annotations.Nullable;

public class ForumTagNameNotUniqueException extends RuntimeException {
    public ForumTagNameNotUniqueException(@Nullable String message) {
        super(message);
    }
}
