package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Plugin.PluginPermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class MappedMountTest {
    @TempDir
    Path temp;

    private Path mapped() throws IOException {
        return Files.createDirectories(temp.resolve("data").resolve("mapped"));
    }

    @Test
    void sameNameIsTheSameDirectory() throws IOException {
        Path mapped = mapped();
        Path first = PluginSecurity.mappedRoot(mapped, "Shared-Cache");
        Path second = PluginSecurity.mappedRoot(mapped, "shared-cache");
        assertEquals(first, second);
        assertTrue(Files.isDirectory(first));
        assertEquals(mapped.toRealPath().resolve("shared-cache"), first);
    }

    @Test
    void invalidAndReservedNamesAreRefused() throws IOException {
        Path mapped = mapped();
        assertThrows(IOException.class, () -> PluginSecurity.mappedRoot(mapped, "../volumes"));
        assertThrows(IOException.class, () -> PluginSecurity.mappedRoot(mapped, "a/b"));
        assertThrows(IOException.class, () -> PluginSecurity.mappedRoot(mapped, ""));
        assertThrows(IOException.class, () -> PluginSecurity.mappedRoot(mapped, "CON"));
        assertThrows(IOException.class, () -> PluginSecurity.mappedRoot(mapped, "lpt1"));
        try (var entries = Files.list(mapped)) {
            assertEquals(0, entries.count());
        }
    }

    @Test
    void linksOutOfTheMappedDirectoryAreRefused() throws IOException {
        Path mapped = mapped();
        Path outside = Files.createDirectories(temp.resolve("outside"));
        try {
            Files.createSymbolicLink(mapped.resolve("escape"), outside);
        } catch (IOException | UnsupportedOperationException e) {
            assumeTrue(false, "Symbolic links aren't available: " + e);
        }
        assertThrows(IOException.class, () -> PluginSecurity.mappedRoot(mapped, "escape"));
    }

    @Test
    void pluginsShareTheMappedDirectory() throws IOException {
        Path shared = PluginSecurity.mappedRoot(mapped(), "exchange");
        PluginSandbox writer = sandbox("Writer", new PluginSandbox.MountPoint("exchange", shared, false));
        PluginSandbox reader = sandbox("Reader", new PluginSandbox.MountPoint("exchange", shared, true));

        Files.writeString(writer.getMount("exchange").resolve("hello.txt"), "hi");
        assertEquals("hi", Files.readString(reader.getMount("exchange").resolve("hello.txt")));
        assertThrows(AccessDeniedException.class,
                () -> Files.writeString(reader.getMount("exchange").resolve("other.txt"), "no"));
    }

    private PluginSandbox sandbox(String name, PluginSandbox.MountPoint mount) throws IOException {
        return new PluginSandbox(name, UUID.randomUUID(), Set.of(), Map.of(mount.name(), mount),
                Files.createDirectories(temp.resolve("volumes").resolve(name)));
    }
}
