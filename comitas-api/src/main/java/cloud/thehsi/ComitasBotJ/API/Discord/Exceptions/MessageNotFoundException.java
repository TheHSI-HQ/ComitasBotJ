package cloud.thehsi.ComitasBotJ.API.Discord.Exceptions;

import org.jetbrains.annotations.Nullable;

public class MessageNotFoundException extends RuntimeException {
    public MessageNotFoundException(@Nullable String message) {
        super(message);
    }
}
