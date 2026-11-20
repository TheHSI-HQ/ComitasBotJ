package cloud.thehsi.ComitasBotJ.Plugin;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown, when a plugin isn't loaded, because one of its dependencies isn't loaded.
 */
public class PluginDependencyException extends RuntimeException {
    public PluginDependencyException(@NotNull String message) {
        super(message);
    }
}
