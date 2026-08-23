package cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum;

import org.jetbrains.annotations.Nullable;

public class ForumTagUsedOnIncorrectChannelException extends RuntimeException {
    public ForumTagUsedOnIncorrectChannelException(@Nullable String message) {
        super(message);
    }
}
