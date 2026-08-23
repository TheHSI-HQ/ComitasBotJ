package cloud.thehsi.ComitasBotJ.Plugin;

import org.jetbrains.annotations.NotNull;

public class PluginCompatibilityException extends RuntimeException {
    public PluginCompatibilityException(@NotNull String targetVersion, @NotNull String currentVersion) {
        super("Plugin only supports " +
                targetVersion +
                ", current version is " +
                currentVersion);
    }
}
