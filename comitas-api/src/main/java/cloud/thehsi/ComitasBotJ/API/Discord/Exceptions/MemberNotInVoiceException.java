package cloud.thehsi.ComitasBotJ.API.Discord.Exceptions;

import org.jetbrains.annotations.Nullable;

/**
 * An exception thrown, if a voice action targets a member, which isn't connected to a voice channel
 */
public class MemberNotInVoiceException extends RuntimeException {
    /**
     * @param message The exception message
     */
    public MemberNotInVoiceException(@Nullable String message) {
        super(message);
    }
}
