package cloud.thehsi.ComitasBotJ.Compatibility.Shims;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Shims for the breaking changes of API 0.0.18, used by plugins built against 0.0.17 or older.
 * <p>
 * Plugins can call these classes (the compatibility layer redirects old calls here), so shims must only use the public API.
 */
@SuppressWarnings("unused")
public final class V0_0_18 {
    private V0_0_18() {
    }

    /**
     * {@code new ConsoleCommand(List, Plugin, String, ConsoleCommandExecutor)}, a tab completer was added.
     */
    @NotNull
    public static ConsoleCommandRegistry.ConsoleCommand newConsoleCommand(@NotNull List<String> aliases, @NotNull Plugin plugin,
                                                                         @NotNull String description,
                                                                         @NotNull ConsoleCommandExecutor executor) {
        return new ConsoleCommandRegistry.ConsoleCommand(aliases, plugin, description, executor, null);
    }
}
