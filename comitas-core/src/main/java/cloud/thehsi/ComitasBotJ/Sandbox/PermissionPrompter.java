package cloud.thehsi.ComitasBotJ.Sandbox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Asks the server owner about plugin permissions and mount points.
 */
@FunctionalInterface
public interface PermissionPrompter {
    /**
     * Never prompts, used when there is no interactive console.
     */
    @NotNull PermissionPrompter NON_INTERACTIVE = (message, prompt) -> CompletableFuture.completedFuture(null);

    /**
     * Shows the message and reads one line.
     *
     * @return The answer, or null if nobody can answer (no console, end of input)
     */
    @NotNull
    CompletableFuture<@Nullable String> ask(@NotNull String message, @NotNull String prompt);
}
