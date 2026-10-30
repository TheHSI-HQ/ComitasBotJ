package cloud.thehsi.ComitasBotJ.Sandbox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Asks the server owner, which permissions a plugin gets.
 */
@FunctionalInterface
public interface PermissionPrompter {
    /**
     * Never prompts, used when there is no interactive console.
     */
    @NotNull PermissionPrompter NON_INTERACTIVE = (message, prompt) -> null;

    /**
     * Shows the message and reads one line.
     *
     * @return The answer, or null if nobody can answer (no console, end of input)
     */
    @Nullable
    String ask(@NotNull String message, @NotNull String prompt);
}
