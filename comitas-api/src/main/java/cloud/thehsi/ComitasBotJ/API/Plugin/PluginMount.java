package cloud.thehsi.ComitasBotJ.API.Plugin;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * A host directory, which the server owner made available to a plugin.
 *
 * @param name     The name of the mount point
 * @param path     The root of the mount point in the plugin's file system ({@code /mnt/<name>})
 * @param readOnly If the plugin may only read from the mount point
 */
public record PluginMount(@NotNull String name, @NotNull Path path, boolean readOnly) {
}
