package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Plugin.PluginPermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PluginLibrariesTest {
    private static final String LIBRARY_PATH = "META-INF/libs/library.jar";

    @TempDir
    Path temp;

    /**
     * {@code testlib.Library.version()} returns {@code System.getProperty("java.version")}, which plugins can't call.
     */
    private static byte[] libraryClass() {
        return generate("testlib/Library", Map.of("version", mv -> {
            mv.visitLdcInsn("java.version");
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "getProperty",
                    "(Ljava/lang/String;)Ljava/lang/String;", false);
            mv.visitInsn(Opcodes.ARETURN);
        }));
    }

    /**
     * {@code viaLibrary()} calls the library, {@code direct()} calls {@code System.getProperty} itself,
     * {@code sql()} returns {@code java.sql.Types.class.getName()}.
     */
    private static byte[] pluginClass() {
        return generate("testplugin/UsesLibrary", Map.of(
                "viaLibrary", mv -> {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "testlib/Library", "version", "()Ljava/lang/String;", false);
                    mv.visitInsn(Opcodes.ARETURN);
                },
                "direct", mv -> {
                    mv.visitLdcInsn("java.version");
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "getProperty",
                            "(Ljava/lang/String;)Ljava/lang/String;", false);
                    mv.visitInsn(Opcodes.ARETURN);
                },
                "sql", mv -> {
                    mv.visitLdcInsn(Type.getObjectType("java/sql/Types"));
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
                    mv.visitInsn(Opcodes.ARETURN);
                }));
    }

    /**
     * A public class with a default constructor and static {@code String} methods without arguments.
     */
    private static byte[] generate(String name, Map<String, Consumer<MethodVisitor>> methods) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, name, null, "java/lang/Object", null);

        MethodVisitor init = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        init.visitCode();
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        init.visitInsn(Opcodes.RETURN);
        init.visitMaxs(0, 0);
        init.visitEnd();

        methods.forEach((method, body) -> {
            MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, method,
                    "()Ljava/lang/String;", null, null);
            mv.visitCode();
            body.accept(mv);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });

        writer.visitEnd();
        return writer.toByteArray();
    }

    private static byte[] jar(Map<String, byte[]> entries) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (JarOutputStream out = new JarOutputStream(bytes)) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                out.putNextEntry(new JarEntry(entry.getKey()));
                out.write(entry.getValue());
                out.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    private File pluginJar(byte[] library) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("testplugin/UsesLibrary.class", pluginClass());
        entries.put(LIBRARY_PATH, library);
        Path jar = temp.resolve("plugin.jar");
        try (OutputStream out = Files.newOutputStream(jar)) {
            out.write(jar(entries));
        }
        return jar.toFile();
    }

    private File pluginJar() throws IOException {
        return pluginJar(jar(Map.of("testlib/Library.class", libraryClass())));
    }

    private PluginSandbox sandbox(PluginPermission... granted) throws IOException {
        Set<PluginPermission> permissions = EnumSet.noneOf(PluginPermission.class);
        permissions.addAll(List.of(granted));
        return new PluginSandbox("LibraryTest", UUID.randomUUID(), permissions, Map.of(),
                Files.createDirectories(temp.resolve("volume")));
    }

    private SandboxPluginClassLoader loader(PluginSandbox sandbox, List<String> libraries) throws IOException {
        return new SandboxPluginClassLoader(pluginJar(), sandbox, libraries, temp.resolve("cache"));
    }

    private static Object call(ClassLoader loader, String method) throws Throwable {
        try {
            return loader.loadClass("testplugin.UsesLibrary").getMethod(method).invoke(null);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Test
    void grantedLibrariesRunUnsandboxed() throws Throwable {
        try (SandboxPluginClassLoader loader = loader(sandbox(PluginPermission.LIBRARIES), List.of(LIBRARY_PATH))) {
            assertEquals(System.getProperty("java.version"), call(loader, "viaLibrary"));
            assertEquals("java.sql.Types", call(loader, "sql"));
            // The plugin's own code stays sandboxed
            assertThrows(SecurityException.class, () -> call(loader, "direct"));
        }
    }

    @Test
    void librariesArentLoadedWithoutThePermission() throws Throwable {
        try (SandboxPluginClassLoader loader = loader(sandbox(), List.of(LIBRARY_PATH))) {
            assertThrows(SecurityException.class, () -> call(loader, "viaLibrary"));
            assertThrows(SecurityException.class, () -> call(loader, "sql"));
        }
        assertFalse(Files.exists(temp.resolve("cache")));
    }

    @Test
    void jdbcIsAvailableWithTheNetworkPermission() throws Throwable {
        try (SandboxPluginClassLoader loader = loader(sandbox(PluginPermission.NETWORK), List.of(LIBRARY_PATH))) {
            assertEquals("java.sql.Types", call(loader, "sql"));
            assertThrows(SecurityException.class, () -> call(loader, "viaLibrary"));
        }
    }

    @Test
    void extractedLibrariesAreReused() throws Throwable {
        PluginSandbox sandbox = sandbox(PluginPermission.LIBRARIES);
        try (SandboxPluginClassLoader first = loader(sandbox, List.of(LIBRARY_PATH));
             SandboxPluginClassLoader second = loader(sandbox, List.of(LIBRARY_PATH))) {
            assertEquals(call(first, "viaLibrary"), call(second, "viaLibrary"));
        }
        try (Stream<Path> files = Files.list(temp.resolve("cache"))) {
            List<Path> jars = files.toList();
            assertEquals(1, jars.size(), jars::toString);
            assertTrue(jars.getFirst().getFileName().toString().matches("[0-9a-f]{64}\\.jar"));
        }
    }

    @Test
    void librariesMustNotBundleJda() throws IOException {
        File plugin = pluginJar(jar(Map.of("net/dv8tion/jda/api/JDA.class", libraryClass())));
        PluginSandbox sandbox = sandbox(PluginPermission.LIBRARIES);
        assertThrows(SandboxViolationException.class, () -> new SandboxPluginClassLoader(plugin, sandbox,
                List.of(LIBRARY_PATH), temp.resolve("cache")).close());
    }

    @Test
    void missingLibrariesFailLoading() throws IOException {
        PluginSandbox sandbox = sandbox(PluginPermission.LIBRARIES);
        assertThrows(IOException.class, () -> loader(sandbox, List.of("META-INF/libs/missing.jar")).close());
    }

    @Test
    void hostPathsNeedTheLibrariesPermission() throws IOException {
        PluginSandbox granted = sandbox(PluginPermission.LIBRARIES);
        assertEquals(temp.resolve("volume").resolve("data.db").toAbsolutePath().toString(),
                granted.hostPath(granted.getDataFolder().resolve("../data.db")));

        PluginSandbox denied = sandbox();
        assertThrows(SecurityException.class, () -> denied.hostPath(denied.getDataFolder().resolve("data.db")));
    }

    @Test
    void librariesAreParsedFromPluginProperties() {
        Properties props = new Properties();
        props.setProperty("libraries", " /META-INF/libs/a.jar, libs/b.jar  libs/b.jar ");
        assertEquals(List.of("META-INF/libs/a.jar", "libs/b.jar"), SandboxPluginClassLoader.parseLibraries(props));
        assertEquals(List.of(), SandboxPluginClassLoader.parseLibraries(new Properties()));
    }
}
