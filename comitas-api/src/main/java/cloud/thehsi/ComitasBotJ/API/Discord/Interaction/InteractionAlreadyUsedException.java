package cloud.thehsi.ComitasBotJ.API.Discord.Interaction;

import org.jetbrains.annotations.Nullable;

public class InteractionAlreadyUsedException extends RuntimeException {
    public InteractionAlreadyUsedException(@Nullable String message) {
        super(message);
    }
}
