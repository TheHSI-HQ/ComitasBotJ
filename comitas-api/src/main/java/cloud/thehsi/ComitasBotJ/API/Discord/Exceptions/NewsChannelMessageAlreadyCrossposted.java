package cloud.thehsi.ComitasBotJ.API.Discord.Exceptions;

import org.jetbrains.annotations.Nullable;

public class NewsChannelMessageAlreadyCrossposted extends RuntimeException {
    public NewsChannelMessageAlreadyCrossposted(@Nullable String message) {
        super(message);
    }
}
