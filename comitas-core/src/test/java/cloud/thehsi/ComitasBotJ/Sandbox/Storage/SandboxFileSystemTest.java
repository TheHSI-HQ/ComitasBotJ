package cloud.thehsi.ComitasBotJ.Sandbox.Storage;

import cloud.thehsi.ComitasBotJ.Sandbox.PluginSandbox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class SandboxFileSystemTest {
    @TempDir
    Path temp;

    private SandboxFileSystem fileSystem(boolean writable, long quota) throws IOException {
        Path data = Files.createDirectories(temp.resolve("data"));
        return new SandboxFileSystem(data.toAbsolutePath(), writable, new ConcurrentHashMap<>(), quota);
    }

    @Test
    void writesLandInTheDataFolder() throws IOException {
        SandboxFileSystem fs = fileSystem(true, 0);
        Files.writeString(fs.getPath("/notes/../note.txt"), "hello");
        assertEquals("hello", Files.readString(temp.resolve("data/note.txt")));
    }

    @Test
    void parentReferencesCantLeaveTheDataFolder() throws IOException {
        SandboxFileSystem fs = fileSystem(true, 0);
        try {
            Files.writeString(fs.getPath("/../../escape.txt"), "x");
        } catch (IOException ignored) {
        }
        assertFalse(Files.exists(temp.resolve("escape.txt")));
    }

    @Test
    void dataFolderIsReadOnlyWithoutPermission() throws IOException {
        SandboxFileSystem fs = fileSystem(false, 0);
        assertThrows(AccessDeniedException.class, () -> Files.writeString(fs.getPath("/note.txt"), "x"));
    }

    @Test
    void readOnlyMountsCanBeReadButNotWritten() throws IOException {
        Path shared = Files.createDirectories(temp.resolve("shared"));
        Files.writeString(shared.resolve("config.txt"), "value");

        Map<String, PluginSandbox.MountPoint> mounts = new ConcurrentHashMap<>();
        mounts.put("shared", new PluginSandbox.MountPoint("shared", shared.toAbsolutePath(), true));
        SandboxFileSystem fs = new SandboxFileSystem(Files.createDirectories(temp.resolve("data")).toAbsolutePath(), true, mounts);

        assertEquals("value", Files.readString(fs.getPath("/mnt/shared/config.txt")));
        assertThrows(AccessDeniedException.class, () -> Files.writeString(fs.getPath("/mnt/shared/config.txt"), "x"));
        assertThrows(NoSuchFileException.class, () -> Files.readString(fs.getPath("/mnt/missing/config.txt")));
    }

    @Test
    void quotaStopsFurtherWrites() throws IOException {
        SandboxFileSystem fs = fileSystem(true, 10);
        Files.writeString(fs.getPath("/big.txt"), "more than ten bytes");

        FileSystemException e = assertThrows(FileSystemException.class, () -> Files.writeString(fs.getPath("/next.txt"), "x"));
        assertFalse(e.getMessage().contains(temp.toString()), "The host path must not leak");

        // Deleting is allowed, so plugins can get below the quota again
        Files.delete(fs.getPath("/big.txt"));
        Files.writeString(fs.getPath("/next.txt"), "x");
    }

    @Test
    void exceptionsDontContainHostPaths() throws IOException {
        SandboxFileSystem fs = fileSystem(true, 0);
        NoSuchFileException e = assertThrows(NoSuchFileException.class, () -> Files.readString(fs.getPath("/missing.txt")));
        assertEquals("/missing.txt", e.getFile());
    }

    @Test
    void mountDirectoryIsHiddenFromTheDataRoot() throws IOException {
        SandboxFileSystem fs = fileSystem(true, 0);
        Files.createDirectories(temp.resolve("data/mnt"));
        Files.writeString(temp.resolve("data/visible.txt"), "x");

        List<String> names = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(fs.getPath("/"))) {
            stream.forEach(path -> names.add(path.getFileName().toString()));
        }
        assertEquals(List.of("visible.txt"), names);
    }

    @Test
    void globCharacterClassesAreEscaped() throws IOException {
        SandboxFileSystem fs = fileSystem(true, 0);

        PathMatcher dotOrA = fs.getPathMatcher("glob:[a.]x");
        assertTrue(dotOrA.matches(fs.getPath("ax")));
        assertTrue(dotOrA.matches(fs.getPath(".x")));
        assertFalse(dotOrA.matches(fs.getPath("bx")));

        PathMatcher negated = fs.getPathMatcher("glob:[!a]x");
        assertTrue(negated.matches(fs.getPath("bx")));
        assertFalse(negated.matches(fs.getPath("ax")));

        PathMatcher range = fs.getPathMatcher("glob:file[0-9].txt");
        assertTrue(range.matches(fs.getPath("file5.txt")));
        assertFalse(range.matches(fs.getPath("filex.txt")));

        // Regex syntax inside the class must not be interpreted, e.g. as an intersection
        PathMatcher literal = fs.getPathMatcher("glob:[&&[]x");
        assertTrue(literal.matches(fs.getPath("&x")));
    }
}
