package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Plugin.PluginMount;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginPermission;
import cloud.thehsi.ComitasBotJ.Sandbox.Storage.SandboxFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The granted capabilities of one plugin.
 */
public final class PluginSandbox {
    private final @NotNull String pluginName;
    private final @NotNull UUID uuid;
    private final @NotNull Set<PluginPermission> granted;
    private final @NotNull Map<String, MountPoint> mounts = new ConcurrentHashMap<>();
    private final @NotNull Set<String> neverMounts = ConcurrentHashMap.newKeySet();
    private final @NotNull Map<String, CompletableFuture<@Nullable Path>> pendingMountRequests = new ConcurrentHashMap<>();
    private final @NotNull AtomicInteger mountRequests = new AtomicInteger();
    private final @NotNull SandboxFileSystem fileSystem;
    private volatile @Nullable PrintStream stdout;
    private volatile @Nullable PrintStream stderr;

    /**
     * @param dataRoot The host directory, the plugin sees as {@code /}
     */
    public PluginSandbox(@NotNull String pluginName, @NotNull UUID uuid, @NotNull Set<PluginPermission> granted,
                         @NotNull Map<String, MountPoint> mounts, @NotNull Path dataRoot) {
        this.pluginName = pluginName;
        this.uuid = uuid;
        this.granted = Set.copyOf(granted);
        this.mounts.putAll(mounts);
        this.fileSystem = new SandboxFileSystem(dataRoot.toAbsolutePath().normalize(),
                has(PluginPermission.FILE_STORAGE), this.mounts, dataQuotaBytes());
    }

    private static long dataQuotaBytes() {
        if (cloud.thehsi.ComitasBotJ.Main.conf() == null) return 0;
        return (long) (cloud.thehsi.ComitasBotJ.Main.conf().pluginDataQuotaMb.get() * 1024 * 1024);
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

    /**
     * The plugin's isolated file system, also returned by {@code Path.of} / {@code FileSystems.getDefault()} in plugin code.
     */
    @NotNull
    public SandboxFileSystem fileSystem() {
        return fileSystem;
    }

    /**
     * Replacement of {@code System.out} in plugin code.
     */
    @NotNull
    PrintStream stdout() {
        PrintStream stream = stdout;
        if (stream == null) {
            synchronized (this) {
                if (stdout == null) stdout = createStream(false);
                stream = stdout;
            }
        }
        return stream;
    }

    /**
     * Replacement of {@code System.err} in plugin code.
     */
    @NotNull
    PrintStream stderr() {
        PrintStream stream = stderr;
        if (stream == null) {
            synchronized (this) {
                if (stderr == null) stderr = createStream(true);
                stream = stderr;
            }
        }
        return stream;
    }

    @NotNull
    private PrintStream createStream(boolean error) {
        Logger logger = LoggerFactory.getLogger("Plugin." + pluginName.replaceAll("[^A-Za-z0-9_-]", "_"));
        return new PrintStream(new PluginLogOutputStream(logger, error), true, StandardCharsets.UTF_8);
    }

    @NotNull
    public Path getDataFolder() {
        return fileSystem.getPath("/");
    }

    @Nullable
    public Path getMount(@NotNull String name) {
        if (!mounts.containsKey(name)) return null;
        return fileSystem.getPath("/mnt", name);
    }

    @NotNull
    @Unmodifiable
    public List<PluginMount> getMounts() {
        return mounts.values().stream()
                .sorted(Comparator.comparing(MountPoint::name))
                .map(mount -> new PluginMount(mount.name(), fileSystem.getPath("/mnt", mount.name()), mount.readOnly()))
                .toList();
    }

    void addMount(@NotNull MountPoint mount) {
        mounts.put(mount.name(), mount);
    }

    boolean isNeverMount(@NotNull String name) {
        return neverMounts.contains(name);
    }

    void addNeverMount(@NotNull String name) {
        neverMounts.add(name);
    }

    @NotNull
    Map<String, CompletableFuture<@Nullable Path>> pendingMountRequests() {
        return pendingMountRequests;
    }

    /**
     * @return How many mount points were requested since the plugin was loaded, including this request
     */
    int countMountRequest() {
        return mountRequests.incrementAndGet();
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
