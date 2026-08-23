package cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread;

import org.jetbrains.annotations.Nullable;

public class TagUsedOnIncorrectChannelException extends RuntimeException {
    public TagUsedOnIncorrectChannelException(@Nullable String message) {
        super(message);
    }
}
