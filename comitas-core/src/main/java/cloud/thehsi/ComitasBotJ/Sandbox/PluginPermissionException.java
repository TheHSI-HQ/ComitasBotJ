package cloud.thehsi.ComitasBotJ.Sandbox;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown, if a plugin can't be loaded, because of its permission settings.
 */
public class PluginPermissionException extends RuntimeException {
    public PluginPermissionException(@NotNull String message) {
        super(message);
    }
}
