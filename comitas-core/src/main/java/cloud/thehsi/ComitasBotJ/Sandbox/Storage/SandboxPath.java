package cloud.thehsi.ComitasBotJ.Sandbox.Storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A path inside a plugin's {@link SandboxFileSystem}, always '/' separated and never pointing to the host directly.
 */
final class SandboxPath implements Path {
    private final @NotNull SandboxFileSystem fs;
    private final boolean absolute;
    private final @NotNull List<String> names;

    SandboxPath(@NotNull SandboxFileSystem fs, boolean absolute, @NotNull List<String> names) {
        this.fs = fs;
        this.absolute = absolute;
        this.names = List.copyOf(names);
    }

    @NotNull
    static SandboxPath parse(@NotNull SandboxFileSystem fs, @NotNull String first, @NotNull String... more) {
        StringBuilder joined = new StringBuilder(first);
        for (String part : more) {
            if (part.isEmpty()) continue;
            if (!joined.isEmpty()) joined.append('/');
            joined.append(part);
        }

        String path = joined.toString();
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c < 0x20 || c == '\\' || c == ':')
                throw new InvalidPathException(path, "Illegal character in plugin path, use '/' as separator", i);
        }

        List<String> names = Arrays.stream(path.split("/")).filter(n -> !n.isEmpty()).toList();
        return new SandboxPath(fs, path.startsWith("/"), names);
    }

    @NotNull
    List<String> names() {
        return names;
    }

    @NotNull
    @Override
    public SandboxFileSystem getFileSystem() {
        return fs;
    }

    @Override
    public boolean isAbsolute() {
        return absolute;
    }

    @Nullable
    @Override
    public Path getRoot() {
        return absolute ? new SandboxPath(fs, true, List.of()) : null;
    }

    @Nullable
    @Override
    public Path getFileName() {
        if (names.isEmpty()) return absolute ? null : this;
        return new SandboxPath(fs, false, List.of(names.getLast()));
    }

    @Nullable
    @Override
    public Path getParent() {
        if (names.isEmpty()) return null;
        if (names.size() == 1) return getRoot();
        return new SandboxPath(fs, absolute, names.subList(0, names.size() - 1));
    }

    @Override
    public int getNameCount() {
        return names.size();
    }

    @NotNull
    @Override
    public Path getName(int index) {
        if (index < 0 || index >= names.size()) throw new IllegalArgumentException("Invalid name index " + index);
        return new SandboxPath(fs, false, List.of(names.get(index)));
    }

    @NotNull
    @Override
    public Path subpath(int beginIndex, int endIndex) {
        if (beginIndex < 0 || endIndex > names.size() || beginIndex >= endIndex)
            throw new IllegalArgumentException("Invalid subpath " + beginIndex + ".." + endIndex);
        return new SandboxPath(fs, false, names.subList(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(@NotNull Path other) {
        if (!(other instanceof SandboxPath o) || o.fs != fs || o.absolute != absolute) return false;
        return o.names.size() <= names.size() && names.subList(0, o.names.size()).equals(o.names);
    }

    @Override
    public boolean endsWith(@NotNull Path other) {
        if (!(other instanceof SandboxPath o) || o.fs != fs) return false;
        if (o.absolute) return equals(o);
        return o.names.size() <= names.size()
                && names.subList(names.size() - o.names.size(), names.size()).equals(o.names);
    }

    @NotNull
    @Override
    public SandboxPath normalize() {
        List<String> result = new ArrayList<>();
        for (String name : names) {
            if (name.equals(".")) continue;
            if (name.equals("..")) {
                if (!result.isEmpty() && !result.getLast().equals("..")) {
                    result.removeLast();
                    continue;
                }
                if (absolute) continue; // can't go above the root
            }
            result.add(name);
        }
        return new SandboxPath(fs, absolute, result);
    }

    @NotNull
    @Override
    public Path resolve(@NotNull Path other) {
        SandboxPath o = checkPath(other);
        if (o.absolute) return o;
        if (o.names.isEmpty()) return this;
        List<String> joined = new ArrayList<>(names);
        joined.addAll(o.names);
        return new SandboxPath(fs, absolute, joined);
    }

    @NotNull
    @Override
    public Path relativize(@NotNull Path other) {
        SandboxPath o = checkPath(other);
        if (o.absolute != absolute) throw new IllegalArgumentException("Can't relativize absolute and relative paths");

        int common = 0;
        while (common < names.size() && common < o.names.size() && names.get(common).equals(o.names.get(common)))
            common++;

        List<String> result = new ArrayList<>();
        for (int i = common; i < names.size(); i++) result.add("..");
        result.addAll(o.names.subList(common, o.names.size()));
        return new SandboxPath(fs, false, result);
    }

    @NotNull
    @Override
    public URI toUri() {
        try {
            return new URI(SandboxFileSystemProvider.SCHEME, null, toAbsolutePath().toString(), null);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @NotNull
    @Override
    public SandboxPath toAbsolutePath() {
        return absolute ? this : new SandboxPath(fs, true, names);
    }

    @NotNull
    @Override
    public Path toRealPath(@NotNull LinkOption @NotNull ... options) throws IOException {
        SandboxPath real = toAbsolutePath().normalize();
        fs.provider().checkAccess(real);
        return real;
    }

    @NotNull
    @Override
    public WatchKey register(@NotNull WatchService watcher, WatchEvent.Kind<?> @NotNull [] events, WatchEvent.Modifier @NotNull ... modifiers) {
        throw new UnsupportedOperationException("Watch services aren't supported in the plugin file system");
    }

    @Override
    public int compareTo(@NotNull Path other) {
        return toString().compareTo(other.toString());
    }

    @NotNull
    SandboxPath checkPath(@NotNull Path other) {
        if (!(other instanceof SandboxPath o) || o.fs != fs) throw new ProviderMismatchException();
        return o;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof SandboxPath o && o.fs == fs && o.absolute == absolute && o.names.equals(names);
    }

    @Override
    public int hashCode() {
        return Objects.hash(System.identityHashCode(fs), absolute, names);
    }

    @NotNull
    @Override
    public String toString() {
        return (absolute ? "/" : "") + String.join("/", names);
    }
}
