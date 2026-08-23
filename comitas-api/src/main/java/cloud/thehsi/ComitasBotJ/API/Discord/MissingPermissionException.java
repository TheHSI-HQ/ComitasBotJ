package cloud.thehsi.ComitasBotJ.API.Discord;

import org.jetbrains.annotations.Nullable;

public class MissingPermissionException extends Exception {
    public MissingPermissionException(@Nullable String message) {
        super(message);
    }
}
