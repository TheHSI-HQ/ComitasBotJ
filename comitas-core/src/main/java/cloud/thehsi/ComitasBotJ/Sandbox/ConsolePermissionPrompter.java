package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.Console.ConsolePrompt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Asks about plugin permissions and mount points on the JLine console.
 */
public final class ConsolePermissionPrompter implements PermissionPrompter {
    private final @NotNull ConsolePrompt consolePrompt;

    public ConsolePermissionPrompter(@NotNull ConsolePrompt consolePrompt) {
        this.consolePrompt = consolePrompt;
    }

    @Override
    @NotNull
    public CompletableFuture<@Nullable String> ask(@NotNull String message, @NotNull String prompt) {
        if (System.console() == null) return CompletableFuture.completedFuture(null);
        return consolePrompt.ask(message, prompt);
    }
}
