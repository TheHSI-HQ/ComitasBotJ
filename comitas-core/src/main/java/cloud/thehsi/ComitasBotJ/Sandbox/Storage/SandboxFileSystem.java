package cloud.thehsi.ComitasBotJ.Sandbox.Storage;

import cloud.thehsi.ComitasBotJ.Sandbox.PluginSandbox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
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

    /**
     * @param dataRoot The absolute host directory the plugin sees as {@code /}
     * @param writable If the plugin may modify its data folder (file_storage permission)
     * @param mounts   The mount points, may be modified later on
     */
    public SandboxFileSystem(@NotNull Path dataRoot, boolean writable, @NotNull Map<String, PluginSandbox.MountPoint> mounts) {
        this.dataRoot = dataRoot;
        this.writable = writable;
        this.mounts = mounts;
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

        Files.createDirectories(dataRoot);
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
                        String content = glob.substring(i + 1, end).replace("\\", "\\\\");
                        if (content.startsWith("!")) content = "^" + content.substring(1);
                        regex.append('[').append(content).append(']');
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
