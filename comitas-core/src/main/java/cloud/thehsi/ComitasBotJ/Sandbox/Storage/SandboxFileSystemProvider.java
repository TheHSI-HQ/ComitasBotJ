package cloud.thehsi.ComitasBotJ.Sandbox.Storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Provider of a {@link SandboxFileSystem}, every operation is mapped onto the host after it was checked.
 * Host paths never leave this class, exceptions are rewritten to only mention sandbox paths.
 */
public final class SandboxFileSystemProvider extends FileSystemProvider {
    static final String SCHEME = "comitas-plugin";
    private static final Set<OpenOption> WRITE_OPTIONS = Set.of(
            StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE,
            StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.DELETE_ON_CLOSE
    );

    private final @NotNull SandboxFileSystem fs;

    SandboxFileSystemProvider(@NotNull SandboxFileSystem fs) {
        this.fs = fs;
    }

    @NotNull
    private SandboxPath check(@NotNull Path path) {
        if (!(path instanceof SandboxPath p) || p.getFileSystem() != fs) throw new ProviderMismatchException();
        return p;
    }

    @NotNull
    private Path host(@NotNull Path path, boolean write) throws IOException {
        SandboxFileSystem.Resolved resolved = fs.resolve(check(path), write);
        if (resolved.host() == null)
            throw new AccessDeniedException(resolved.path().toString(), null, "the mount directory is virtual");
        return resolved.host();
    }

    private static boolean writes(@NotNull Collection<? extends OpenOption> options) {
        for (OpenOption option : options)
            if (WRITE_OPTIONS.contains(option)) return true;
        return false;
    }

    /**
     * Rewrites host exceptions, so that host paths aren't exposed to plugins.
     */
    @NotNull
    private static IOException translate(@NotNull IOException e, @NotNull Path a, @Nullable Path b) {
        String file = a.toString();
        String other = b == null ? null : b.toString();
        if (!(e instanceof FileSystemException fse)) return new IOException("I/O error accessing " + file);
        String reason = fse.getReason();
        return switch (e) {
            case NoSuchFileException ignored -> new NoSuchFileException(file, other, null);
            case FileAlreadyExistsException ignored -> new FileAlreadyExistsException(other != null ? other : file);
            case DirectoryNotEmptyException ignored -> new DirectoryNotEmptyException(file);
            case NotDirectoryException ignored -> new NotDirectoryException(file);
            case AccessDeniedException ignored -> new AccessDeniedException(file, other, reason);
            case AtomicMoveNotSupportedException ignored -> new AtomicMoveNotSupportedException(file, other, reason);
            case FileSystemLoopException ignored -> new FileSystemLoopException(file);
            default -> new FileSystemException(file, other, reason);
        };
    }

    @NotNull
    @Override
    public String getScheme() {
        return SCHEME;
    }

    @NotNull
    @Override
    public FileSystem newFileSystem(@NotNull URI uri, @NotNull Map<String, ?> env) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public FileSystem newFileSystem(@NotNull Path path, @NotNull Map<String, ?> env) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public FileSystem getFileSystem(@NotNull URI uri) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Path getPath(@NotNull URI uri) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public SeekableByteChannel newByteChannel(@NotNull Path path, @NotNull Set<? extends OpenOption> options,
                                              @NotNull FileAttribute<?>... attrs) throws IOException {
        Path host = host(path, writes(options));
        try {
            return Files.newByteChannel(host, options, attrs);
        } catch (IOException e) {
            throw translate(e, path, null);
        }
    }

    @NotNull
    @Override
    public FileChannel newFileChannel(@NotNull Path path, @NotNull Set<? extends OpenOption> options,
                                      @NotNull FileAttribute<?>... attrs) throws IOException {
        Path host = host(path, writes(options));
        try {
            return FileChannel.open(host, options, attrs);
        } catch (IOException e) {
            throw translate(e, path, null);
        }
    }

    @NotNull
    @Override
    public AsynchronousFileChannel newAsynchronousFileChannel(@NotNull Path path, @NotNull Set<? extends OpenOption> options,
                                                              @Nullable ExecutorService executor,
                                                              @NotNull FileAttribute<?>... attrs) throws IOException {
        Path host = host(path, writes(options));
        try {
            return AsynchronousFileChannel.open(host, options, executor, attrs);
        } catch (IOException e) {
            throw translate(e, path, null);
        }
    }

    @NotNull
    @Override
    public InputStream newInputStream(@NotNull Path path, @NotNull OpenOption... options) throws IOException {
        if (writes(List.of(options))) throw new UnsupportedOperationException("Write option on an input stream");
        Path host = host(path, false);
        try {
            return Files.newInputStream(host, options);
        } catch (IOException e) {
            throw translate(e, path, null);
        }
    }

    @NotNull
    @Override
    public OutputStream newOutputStream(@NotNull Path path, @NotNull OpenOption... options) throws IOException {
        Path host = host(path, true);
        try {
            return Files.newOutputStream(host, options);
        } catch (IOException e) {
            throw translate(e, path, null);
        }
    }

    @NotNull
    @Override
    public DirectoryStream<Path> newDirectoryStream(@NotNull Path dir, @NotNull DirectoryStream.Filter<? super Path> filter) throws IOException {
        SandboxFileSystem.Resolved resolved = fs.resolve(check(dir), false);
        SandboxPath base = check(dir);

        if (resolved.host() == null) {
            List<Path> entries = new ArrayList<>();
            for (String name : fs.mounts().keySet()) {
                Path entry = base.resolve(name);
                if (filter.accept(entry)) entries.add(entry);
            }
            return new DirectoryStream<>() {
                @NotNull
                @Override
                public Iterator<Path> iterator() {
                    return entries.iterator();
                }

                @Override
                public void close() {
                }
            };
        }

        DirectoryStream<Path> hostStream;
        try {
            hostStream = Files.newDirectoryStream(resolved.host());
        } catch (IOException e) {
            throw translate(e, dir, null);
        }

        boolean dataRoot = resolved.areaRoot() && resolved.path().getNameCount() == 0;
        return new DirectoryStream<>() {
            @NotNull
            @Override
            public Iterator<Path> iterator() {
                Iterator<Path> hostIterator = hostStream.iterator();
                return new Iterator<>() {
                    private @Nullable Path next;

                    @Override
                    public boolean hasNext() {
                        while (next == null && hostIterator.hasNext()) {
                            String name = Objects.requireNonNull(hostIterator.next().getFileName()).toString();
                            // /mnt is reserved for mount points
                            if (dataRoot && name.equals(SandboxFileSystem.MOUNT_DIRECTORY)) continue;
                            Path candidate;
                            try {
                                candidate = base.resolve(name);
                            } catch (InvalidPathException e) {
                                continue;
                            }
                            try {
                                if (filter.accept(candidate)) next = candidate;
                            } catch (IOException e) {
                                throw new DirectoryIteratorException(e);
                            }
                        }
                        return next != null;
                    }

                    @Override
                    public @Nullable Path next() {
                        if (!hasNext()) throw new NoSuchElementException();
                        Path result = next;
                        next = null;
                        return result;
                    }
                };
            }

            @Override
            public void close() throws IOException {
                hostStream.close();
            }
        };
    }

    @Override
    public void createDirectory(@NotNull Path dir, @NotNull FileAttribute<?>... attrs) throws IOException {
        Path host = host(dir, true);
        try {
            Files.createDirectory(host, attrs);
        } catch (IOException e) {
            throw translate(e, dir, null);
        }
    }

    @Override
    public void delete(@NotNull Path path) throws IOException {
        SandboxFileSystem.Resolved resolved = fs.resolve(check(path), true);
        if (resolved.areaRoot() || resolved.host() == null)
            throw new AccessDeniedException(resolved.path().toString(), null, "can't delete a root directory");
        try {
            Files.delete(resolved.host());
        } catch (IOException e) {
            throw translate(e, path, null);
        }
    }

    @Override
    public void copy(@NotNull Path source, @NotNull Path target, @NotNull CopyOption... options) throws IOException {
        Path from = host(source, false);
        SandboxFileSystem.Resolved to = fs.resolve(check(target), true);
        if (to.areaRoot() || to.host() == null)
            throw new AccessDeniedException(target.toString(), null, "can't replace a root directory");
        try {
            Files.copy(from, to.host(), options);
        } catch (IOException e) {
            throw translate(e, source, target);
        }
    }

    @Override
    public void move(@NotNull Path source, @NotNull Path target, @NotNull CopyOption... options) throws IOException {
        SandboxFileSystem.Resolved from = fs.resolve(check(source), true);
        SandboxFileSystem.Resolved to = fs.resolve(check(target), true);
        if (from.areaRoot() || from.host() == null || to.areaRoot() || to.host() == null)
            throw new AccessDeniedException(source.toString(), target.toString(), "can't move a root directory");
        try {
            Files.move(from.host(), to.host(), options);
        } catch (IOException e) {
            throw translate(e, source, target);
        }
    }

    @Override
    public boolean isSameFile(@NotNull Path path, @NotNull Path path2) throws IOException {
        if (path.equals(path2)) return true;
        if (!(path2 instanceof SandboxPath p2) || p2.getFileSystem() != fs) return false;
        SandboxFileSystem.Resolved a = fs.resolve(check(path), false);
        SandboxFileSystem.Resolved b = fs.resolve(p2, false);
        if (a.host() == null || b.host() == null) return a.path().equals(b.path());
        try {
            return Files.isSameFile(a.host(), b.host());
        } catch (IOException e) {
            throw translate(e, path, path2);
        }
    }

    @Override
    public boolean isHidden(@NotNull Path path) throws IOException {
        SandboxFileSystem.Resolved resolved = fs.resolve(check(path), false);
        if (resolved.host() == null) return false;
        try {
            return Files.isHidden(resolved.host());
        } catch (IOException e) {
            throw translate(e, path, null);
        }
    }

    @NotNull
    @Override
    public FileStore getFileStore(@NotNull Path path) {
        throw new UnsupportedOperationException("File stores aren't available in the plugin file system");
    }

    @Override
    public void checkAccess(@NotNull Path path, @NotNull AccessMode... modes) throws IOException {
        boolean write = Arrays.asList(modes).contains(AccessMode.WRITE);
        SandboxFileSystem.Resolved resolved = fs.resolve(check(path), write);
        if (resolved.host() == null) {
            if (Arrays.asList(modes).contains(AccessMode.EXECUTE))
                throw new AccessDeniedException(path.toString());
            return;
        }
        try {
            resolved.host().getFileSystem().provider().checkAccess(resolved.host(), modes);
        } catch (IOException e) {
            throw translate(e, path, null);
        }
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <V extends FileAttributeView> V getFileAttributeView(@NotNull Path path, @NotNull Class<V> type,
                                                                @NotNull LinkOption... options) {
        if (type != BasicFileAttributeView.class) return null;
        SandboxPath sandboxPath = check(path);
        return (V) new BasicFileAttributeView() {
            @NotNull
            @Override
            public String name() {
                return "basic";
            }

            @NotNull
            @Override
            public BasicFileAttributes readAttributes() throws IOException {
                return SandboxFileSystemProvider.this.readAttributes(sandboxPath, BasicFileAttributes.class, options);
            }

            @Override
            public void setTimes(@Nullable FileTime lastModifiedTime, @Nullable FileTime lastAccessTime,
                                 @Nullable FileTime createTime) throws IOException {
                Path host = host(sandboxPath, true);
                try {
                    Files.getFileAttributeView(host, BasicFileAttributeView.class, options)
                            .setTimes(lastModifiedTime, lastAccessTime, createTime);
                } catch (IOException e) {
                    throw translate(e, sandboxPath, null);
                }
            }
        };
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <A extends BasicFileAttributes> A readAttributes(@NotNull Path path, @NotNull Class<A> type,
                                                            @NotNull LinkOption... options) throws IOException {
        if (type != BasicFileAttributes.class)
            throw new UnsupportedOperationException("Only basic file attributes are supported in the plugin file system");

        SandboxFileSystem.Resolved resolved = fs.resolve(check(path), false);
        if (resolved.host() == null) return (A) VirtualDirectoryAttributes.INSTANCE;
        try {
            return (A) Files.readAttributes(resolved.host(), BasicFileAttributes.class, options);
        } catch (IOException e) {
            throw translate(e, path, null);
        }
    }

    @NotNull
    private static String basicAttributes(@NotNull String attributes) {
        int colon = attributes.indexOf(':');
        if (colon >= 0) {
            if (!attributes.substring(0, colon).equals("basic"))
                throw new UnsupportedOperationException("Only basic file attributes are supported in the plugin file system");
            attributes = attributes.substring(colon + 1);
        }
        return "basic:" + attributes;
    }

    @NotNull
    @Override
    public Map<String, Object> readAttributes(@NotNull Path path, @NotNull String attributes,
                                              @NotNull LinkOption... options) throws IOException {
        String basic = basicAttributes(attributes);
        Path host = host(path, false);
        try {
            return Files.readAttributes(host, basic, options);
        } catch (IOException e) {
            throw translate(e, path, null);
        }
    }

    @Override
    public void setAttribute(@NotNull Path path, @NotNull String attribute, @Nullable Object value,
                             @NotNull LinkOption... options) throws IOException {
        String basic = basicAttributes(attribute);
        Path host = host(path, true);
        try {
            Files.setAttribute(host, basic, value, options);
        } catch (IOException e) {
            throw translate(e, path, null);
        }
    }

    @Override
    public void createSymbolicLink(@NotNull Path link, @NotNull Path target, @NotNull FileAttribute<?>... attrs) {
        throw new UnsupportedOperationException("Links aren't supported in the plugin file system");
    }

    @Override
    public void createLink(@NotNull Path link, @NotNull Path existing) {
        throw new UnsupportedOperationException("Links aren't supported in the plugin file system");
    }

    @NotNull
    @Override
    public Path readSymbolicLink(@NotNull Path link) {
        throw new UnsupportedOperationException("Links aren't supported in the plugin file system");
    }

    /**
     * Checks that a path exists and is accessible, used by {@link SandboxPath#toRealPath(LinkOption...)}.
     */
    void checkAccess(@NotNull SandboxPath path) throws IOException {
        checkAccess(path, new AccessMode[0]);
    }

    private enum VirtualDirectoryAttributes implements BasicFileAttributes {
        INSTANCE;

        private static final FileTime EPOCH = FileTime.fromMillis(0);

        @NotNull
        @Override
        public FileTime lastModifiedTime() {
            return EPOCH;
        }

        @NotNull
        @Override
        public FileTime lastAccessTime() {
            return EPOCH;
        }

        @NotNull
        @Override
        public FileTime creationTime() {
            return EPOCH;
        }

        @Override
        public boolean isRegularFile() {
            return false;
        }

        @Override
        public boolean isDirectory() {
            return true;
        }

        @Override
        public boolean isSymbolicLink() {
            return false;
        }

        @Override
        public boolean isOther() {
            return false;
        }

        @Override
        public long size() {
            return 0;
        }

        @Nullable
        @Override
        public Object fileKey() {
            return null;
        }
    }
}
