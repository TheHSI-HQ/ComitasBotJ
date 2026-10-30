package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Plugin.PluginPermission;
import cloud.thehsi.ComitasBotJ.Sandbox.Storage.SandboxFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The granted capabilities of one plugin.
 */
public final class PluginSandbox {
    private final @NotNull String pluginName;
    private final @NotNull UUID uuid;
    private final @NotNull Set<PluginPermission> granted;
    private final @NotNull Map<String, MountPoint> mounts;
    private final @NotNull Path dataRoot;
    private volatile @Nullable SandboxFileSystem fileSystem;

    public PluginSandbox(@NotNull String pluginName, @NotNull UUID uuid, @NotNull Set<PluginPermission> granted,
                         @NotNull Map<String, MountPoint> mounts, @NotNull Path dataRoot) {
        this.pluginName = pluginName;
        this.uuid = uuid;
        this.granted = Set.copyOf(granted);
        this.mounts = Map.copyOf(mounts);
        this.dataRoot = dataRoot.toAbsolutePath().normalize();
    }

    @NotNull
    public String pluginName() {
        return pluginName;
    }

    @NotNull
    public UUID uuid() {
        return uuid;
    }

    public boolean has(@NotNull PluginPermission permission) {
        return granted.contains(permission);
    }

    @NotNull
    public Set<PluginPermission> granted() {
        return granted;
    }

    @NotNull
    public Map<String, MountPoint> mounts() {
        return mounts;
    }

    @NotNull
    public Path getDataFolder() {
        if (!has(PluginPermission.FILE_STORAGE))
            throw new SecurityException("Plugin " + pluginName + " wasn't granted the "
                    + PluginPermission.FILE_STORAGE.getKey() + " permission");
        try {
            Files.createDirectories(dataRoot);
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't create the data folder of " + pluginName, e);
        }
        return fileSystem().getPath("/");
    }

    @Nullable
    public Path getMount(@NotNull String name) {
        if (!mounts.containsKey(name)) return null;
        return fileSystem().getPath("/mnt", name);
    }

    @NotNull
    private SandboxFileSystem fileSystem() {
        SandboxFileSystem fs = fileSystem;
        if (fs == null) {
            synchronized (this) {
                fs = fileSystem;
                if (fs == null)
                    fileSystem = fs = new SandboxFileSystem(
                            has(PluginPermission.FILE_STORAGE) ? dataRoot : null, mounts);
            }
        }
        return fs;
    }

    /**
     * A host directory, made available to a plugin by the server owner.
     *
     * @param name     The name, visible as {@code /mnt/<name>}
     * @param hostRoot The real (absolute, symlink free) host directory
     * @param readOnly If the plugin may only read
     */
    public record MountPoint(@NotNull String name, @NotNull Path hostRoot, boolean readOnly) {
    }
}
