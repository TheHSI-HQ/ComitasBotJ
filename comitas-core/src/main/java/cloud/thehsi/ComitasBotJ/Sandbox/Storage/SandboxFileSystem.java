package cloud.thehsi.ComitasBotJ.Sandbox.Storage;

import cloud.thehsi.ComitasBotJ.Sandbox.PluginSandbox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The isolated file system of one plugin.
 * <p>
 * {@code /} is the plugin's data folder ({@code data/volumes/<uuid>/}, always readable, writable only with the file_storage permission),
 * {@code /mnt/<name>} are the mount points configured by the server owner.
 * Paths of this file system can't be turned into host paths, so plugins can never leave these directories.
 */
public final class SandboxFileSystem extends FileSystem {
    static final String MOUNT_DIRECTORY = "mnt";
    private static final Pattern WINDOWS_DEVICE = Pattern.compile("(?i)(con|prn|aux|nul|com[0-9¹²³]|lpt[0-9¹²³])(\\..*)?");
    private static final boolean WINDOWS = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");

    private final @NotNull SandboxFileSystemProvider provider = new SandboxFileSystemProvider(this);
    private final @NotNull Path dataRoot;
    private final boolean writable;
    private final @NotNull Map<String, PluginSandbox.MountPoint> mounts;
    private final long quotaBytes;
    private volatile long cachedUsage = -1;
    private volatile long usageMeasuredAt;

    /**
     * @param dataRoot The absolute host directory the plugin sees as {@code /}
     * @param writable If the plugin may modify its data folder (file_storage permission)
     * @param mounts   The mount points, may be modified later on
     */
    public SandboxFileSystem(@NotNull Path dataRoot, boolean writable, @NotNull Map<String, PluginSandbox.MountPoint> mounts) {
        this(dataRoot, writable, mounts, 0);
    }

    /**
     * @param quotaBytes The maximum size of the data folder, 0 or less disables the limit
     */
    public SandboxFileSystem(@NotNull Path dataRoot, boolean writable, @NotNull Map<String, PluginSandbox.MountPoint> mounts, long quotaBytes) {
        this.dataRoot = dataRoot;
        this.writable = writable;
        this.mounts = mounts;
        this.quotaBytes = quotaBytes;
    }

    /**
     * Refuses to create or write files in the data folder, once it reached its quota.
     * The limit is checked when a file is opened, so a single stream can still go past it.
     */
    void checkQuota(@NotNull Resolved resolved) throws IOException {
        if (quotaBytes <= 0 || resolved.host() == null || !resolved.host().startsWith(dataRoot)) return;
        if (dataUsage() >= quotaBytes)
            throw new FileSystemException(resolved.path().toString(), null,
                    "the plugin data folder reached its quota of " + quotaBytes / 1024 / 1024 + " MB");
    }

    private long dataUsage() throws IOException {
        long now = System.currentTimeMillis();
        // Cached briefly, walking big folders on every write would be slow
        if (cachedUsage >= 0 && now - usageMeasuredAt < 5_000) return cachedUsage;

        long[] total = {0};
        try {
            Files.walkFileTree(dataRoot, new SimpleFileVisitor<>() {
                @NotNull
                @Override
                public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) {
                    total[0] += attrs.size();
                    return FileVisitResult.CONTINUE;
                }

                @NotNull
                @Override
                public FileVisitResult visitFileFailed(@NotNull Path file, @NotNull IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new FileSystemException("/", null, "the size of the data folder couldn't be determined");
        }

        cachedUsage = total[0];
        usageMeasuredAt = now;
        return total[0];
    }

    @NotNull
    Map<String, PluginSandbox.MountPoint> mounts() {
        return mounts;
    }

    /**
     * Maps a sandbox path onto the host.
     *
     * @param write If the operation modifies the file system
     * @return The resolved host location, or a {@link Resolved} with a null host, for the virtual {@code /mnt} directory
     */
    @NotNull
    Resolved resolve(@NotNull SandboxPath path, boolean write) throws IOException {
        SandboxPath absolute = path.toAbsolutePath().normalize();
        List<String> names = absolute.names();
        String display = absolute.toString();

        if (!names.isEmpty() && names.getFirst().equals(MOUNT_DIRECTORY)) {
            if (names.size() == 1) {
                if (write) throw new AccessDeniedException(display, null, "the mount directory is read-only");
                return new Resolved(absolute, null, true);
            }

            PluginSandbox.MountPoint mount = mounts.get(names.get(1));
            if (mount == null) throw new NoSuchFileException(display);
            if (write && mount.readOnly())
                throw new AccessDeniedException(display, null, "mount point " + mount.name() + " is read-only");

            return new Resolved(absolute, mapUnder(mount.hostRoot(), names.subList(2, names.size()), display), names.size() == 2);
        }

        if (write && !writable)
            throw new AccessDeniedException(display, null, "the plugin wasn't granted the file_storage permission");

        try {
            Files.createDirectories(dataRoot);
        } catch (IOException e) {
            // The host exception would contain the host path
            throw new FileSystemException(display, null, "the data folder couldn't be created");
        }
        return new Resolved(absolute, mapUnder(dataRoot, names, display), names.isEmpty());
    }

    @NotNull
    private static Path mapUnder(@NotNull Path root, @NotNull List<String> names, @NotNull String display) throws IOException {
        Path host = root;
        for (String name : names) {
            if (name.equals("..") || name.equals(".") || name.contains("/") || name.contains("\\") || name.contains(":"))
                throw new AccessDeniedException(display, null, "illegal path element");
            if (WINDOWS && WINDOWS_DEVICE.matcher(name.strip()).matches())
                throw new AccessDeniedException(display, null, "reserved file name");
            host = host.resolve(name);
        }

        host = host.normalize();
        if (!host.startsWith(root)) throw new AccessDeniedException(display, null, "path leaves the sandbox");

        // Symlinks / junctions placed on the host must not lead outside either
        try {
            Path realRoot = root.toRealPath();
            Path existing = host;
            while (existing != null && !Files.exists(existing, LinkOption.NOFOLLOW_LINKS))
                existing = existing.getParent();
            if (existing == null || !existing.toRealPath().startsWith(realRoot))
                throw new AccessDeniedException(display, null, "path leaves the sandbox");
        } catch (AccessDeniedException e) {
            throw e;
        } catch (IOException e) {
            throw new AccessDeniedException(display, null, "path could not be verified");
        }

        return host;
    }

    @NotNull
    @Override
    public SandboxFileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("The plugin file system can't be closed");
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @NotNull
    @Override
    public String getSeparator() {
        return "/";
    }

    @NotNull
    @Override
    public Iterable<Path> getRootDirectories() {
        return List.of(getPath("/"));
    }

    @NotNull
    @Override
    public Iterable<FileStore> getFileStores() {
        return List.of();
    }

    @NotNull
    @Override
    public Set<String> supportedFileAttributeViews() {
        return Set.of("basic");
    }

    @NotNull
    @Override
    public SandboxPath getPath(@NotNull String first, @NotNull String... more) {
        return SandboxPath.parse(this, first, more);
    }

    @NotNull
    @Override
    public PathMatcher getPathMatcher(@NotNull String syntaxAndPattern) {
        int colon = syntaxAndPattern.indexOf(':');
        if (colon <= 0) throw new IllegalArgumentException("Expected syntax:pattern, got " + syntaxAndPattern);

        String syntax = syntaxAndPattern.substring(0, colon);
        String pattern = syntaxAndPattern.substring(colon + 1);
        Pattern regex = switch (syntax.toLowerCase(Locale.ROOT)) {
            case "glob" -> Pattern.compile(globToRegex(pattern));
            case "regex" -> Pattern.compile(pattern);
            default -> throw new UnsupportedOperationException("Unsupported path matcher syntax " + syntax);
        };
        return path -> regex.matcher(path.toString()).matches();
    }

    @NotNull
    private static String globToRegex(@NotNull String glob) {
        StringBuilder regex = new StringBuilder();
        boolean inGroup = false;
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            switch (c) {
                case '*' -> {
                    if (i + 1 < glob.length() && glob.charAt(i + 1) == '*') {
                        regex.append(".*");
                        i++;
                    } else {
                        regex.append("[^/]*");
                    }
                }
                case '?' -> regex.append("[^/]");
                case '{' -> {
                    regex.append("(?:");
                    inGroup = true;
                }
                case '}' -> {
                    regex.append(')');
                    inGroup = false;
                }
                case ',' -> regex.append(inGroup ? "|" : ",");
                case '[' -> {
                    int end = glob.indexOf(']', i + 1);
                    if (end < 0) {
                        regex.append("\\[");
                    } else {
                        regex.append('[').append(characterClass(glob.substring(i + 1, end))).append(']');
                        i = end;
                    }
                }
                case '\\' -> {
                    if (i + 1 < glob.length()) regex.append(Pattern.quote(String.valueOf(glob.charAt(++i))));
                }
                default -> regex.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return regex.toString();
    }

    /**
     * Escapes the content of a glob {@code [...]}, only {@code !} (negation) and {@code -} (ranges) keep their meaning.
     */
    @NotNull
    private static String characterClass(@NotNull String content) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (i == 0 && c == '!' && content.length() > 1) result.append('^');
            else if (c == '-' && i > 0 && i < content.length() - 1) result.append('-');
            else if (Character.isLetterOrDigit(c)) result.append(c);
            else result.append('\\').append(c);
        }
        return result.toString();
    }

    @NotNull
    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException("Not supported in the plugin file system");
    }

    @NotNull
    @Override
    public WatchService newWatchService() {
        throw new UnsupportedOperationException("Watch services aren't supported in the plugin file system");
    }

    /**
     * @param path     The normalized, absolute sandbox path
     * @param host     The host path, null for the virtual {@code /mnt} directory
     * @param areaRoot If this is the data folder root or the root of a mount point (or {@code /mnt})
     */
    record Resolved(@NotNull SandboxPath path, @Nullable Path host, boolean areaRoot) {
    }
}
