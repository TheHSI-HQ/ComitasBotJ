package cloud.thehsi.ComitasBotJ.Sandbox;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown when a plugin tries to do something, the plugin sandbox doesn't allow.
 */
public class SandboxViolationException extends SecurityException {
    public SandboxViolationException(@NotNull String message) {
        super(message);
    }

    public SandboxViolationException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
