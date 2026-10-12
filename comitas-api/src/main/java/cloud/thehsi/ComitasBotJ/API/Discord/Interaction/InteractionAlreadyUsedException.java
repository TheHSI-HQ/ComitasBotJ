package cloud.thehsi.ComitasBotJ.API.Discord.Interaction;

import org.jetbrains.annotations.Nullable;

/**
 * An Exception thrown incase an interaction was used after having already been used
 */
public class InteractionAlreadyUsedException extends RuntimeException {
    /**
     * @param message The exception message
     */
    public InteractionAlreadyUsedException(@Nullable String message) {
        super(message);
    }
}
