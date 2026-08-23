package cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread;

import org.jetbrains.annotations.Nullable;

public class TagNameNotUniqueException extends RuntimeException {
    public TagNameNotUniqueException(@Nullable String message) {
        super(message);
    }
}
