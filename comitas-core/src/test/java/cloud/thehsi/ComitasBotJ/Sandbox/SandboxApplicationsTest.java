package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Plugin.JavaApplication;
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
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sandboxed libraries ({@link PluginPermission#LIBRARIES}), Java applications
 * ({@link PluginPermission#JAVA_APPLICATIONS}) and the JDK's HTTP server.
 */
class SandboxApplicationsTest {
    private static final String LIBRARY_PATH = "META-INF/libs/server.jar";

    @TempDir
    Path temp;

    // --- class generation ---

    /**
     * A public class with a default constructor and the given static methods (name -> descriptor + body).
     */
    private static byte[] generate(String name, Map<String, Map.Entry<String, Consumer<MethodVisitor>>> methods) {
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
            MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, method, body.getKey(), null, null);
            mv.visitCode();
            body.getValue().accept(mv);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        });

        writer.visitEnd();
        return writer.toByteArray();
    }

    @SuppressWarnings("ConfusingMainMethod")
    private static Map.Entry<String, Consumer<MethodVisitor>> main(Consumer<MethodVisitor> body) {
        return Map.entry("([Ljava/lang/String;)V", mv -> {
            body.accept(mv);
            mv.visitInsn(Opcodes.RETURN);
        });
    }

    private static Map.Entry<String, Consumer<MethodVisitor>> string(Consumer<MethodVisitor> body) {
        return Map.entry("()Ljava/lang/String;", mv -> {
            body.accept(mv);
            mv.visitInsn(Opcodes.ARETURN);
        });
    }

    private static Consumer<MethodVisitor> className(String internalName) {
        return mv -> {
            mv.visitLdcInsn(Type.getObjectType(internalName));
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
        };
    }

    private static void getProperty(MethodVisitor mv) {
        mv.visitLdcInsn("java.version");
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "getProperty",
                "(Ljava/lang/String;)Ljava/lang/String;", false);
    }

    private static void exit(MethodVisitor mv) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "exit", "(I)V", false);
    }

    private static byte[] jar(Map<String, byte[]> entries, Map<String, String> manifest) throws IOException {
        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.forEach((key, value) -> mf.getMainAttributes().putValue(key, value));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (JarOutputStream out = new JarOutputStream(bytes, mf)) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                out.putNextEntry(new JarEntry(entry.getKey()));
                out.write(entry.getValue());
                out.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    private static byte[] jar(Map<String, byte[]> entries) throws IOException {
        return jar(entries, Map.of());
    }

    // --- fixtures ---

    /**
     * {@code testserver.Server}: {@code name()} returns a constant, {@code version()} calls {@code System.getProperty}.
     */
    private static byte[] libraryJar() throws IOException {
        return jar(Map.of("testserver/Server.class", generate("testserver/Server", Map.of(
                "name", string(mv -> mv.visitLdcInsn("server")),
                "version", string(SandboxApplicationsTest::getProperty)))));
    }

    private static Object call(ClassLoader loader, String method) throws Throwable {
        try {
            return loader.loadClass("testplugin.Plugin").getMethod(method).invoke(null);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private static ClassLoader appLoader(SandboxJavaApplication application) throws ReflectiveOperationException {
        var field = SandboxJavaApplication.class.getDeclaredField("loader");
        field.setAccessible(true);
        return (ClassLoader) field.get(application);
    }

    private PluginSandbox sandbox(PluginPermission... granted) throws IOException {
        Set<PluginPermission> permissions = EnumSet.noneOf(PluginPermission.class);
        permissions.addAll(List.of(granted));
        return new PluginSandbox("ApplicationTest", UUID.randomUUID(), permissions, Map.of(),
                Files.createDirectories(temp.resolve("volume")));
    }

    /**
     * {@code testplugin.Plugin}: calls the library, and references the JDK's HTTP server.
     */
    private File pluginJar() throws IOException {
        byte[] plugin = generate("testplugin/Plugin", Map.of(
                "name", string(mv -> mv.visitMethodInsn(Opcodes.INVOKESTATIC, "testserver/Server", "name",
                        "()Ljava/lang/String;", false)),
                "version", string(mv -> mv.visitMethodInsn(Opcodes.INVOKESTATIC, "testserver/Server", "version",
                        "()Ljava/lang/String;", false)),
                "httpServer", string(className("com/sun/net/httpserver/HttpServer")),
                "fileServer", string(className("com/sun/net/httpserver/SimpleFileServer"))));

        Path jar = temp.resolve("plugin.jar");
        Files.write(jar, jar(Map.of("testplugin/Plugin.class", plugin, LIBRARY_PATH, libraryJar())));
        return jar.toFile();
    }

    private SandboxPluginClassLoader loader(PluginSandbox sandbox) throws IOException {
        return new SandboxPluginClassLoader(pluginJar(), sandbox, List.of(), List.of(LIBRARY_PATH),
                temp.resolve("cache"), null);
    }

    /**
     * Writes an application jar into the plugin's volume.
     *
     * @return Its path in the plugin's file system
     */
    private Path application(PluginSandbox sandbox, String name, Map<String, byte[]> classes,
                             Map<String, String> manifest) throws IOException {
        Path host = temp.resolve("volume").resolve("apps").resolve(name);
        Files.createDirectories(host.getParent());
        Files.write(host, jar(classes, manifest));
        return sandbox.getDataFolder().resolve("apps/" + name);
    }

    // --- sandboxed libraries ---

    private Path exitApplication(PluginSandbox sandbox) throws IOException {
        // System.exit(args.length)
        byte[] main = generate("app/Main", Map.of("main", main(mv -> {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
            exit(mv);
        })));
        return application(sandbox, "exit.jar", Map.of("app/Main.class", main), Map.of("Main-Class", "app.Main"));
    }

    @Test
    void librariesAreSandboxed() throws Throwable {
        try (SandboxPluginClassLoader loader = loader(sandbox(PluginPermission.LIBRARIES))) {
            assertEquals("server", call(loader, "name"));
            // The library only has the plugin's permissions
            assertThrows(SecurityException.class, () -> call(loader, "version"));
            // Library classes belong to the plugin's class loader
            assertSame(loader, loader.loadClass("testserver.Server").getClassLoader());
        }
    }

    @Test
    void librariesArentLoadedWithoutThePermission() throws Throwable {
        try (SandboxPluginClassLoader loader = loader(sandbox(PluginPermission.RISKY_LIBRARIES))) {
            assertThrows(SecurityException.class, () -> call(loader, "name"));
        }
        assertFalse(Files.exists(temp.resolve("cache")));
    }

    @Test
    void librariesAreParsedFromPluginProperties() {
        Properties props = new Properties();
        props.setProperty("libraries", "/META-INF/libs/a.jar libs/b.jar");
        props.setProperty("risky-libraries", "libs/c.jar");
        assertEquals(List.of("META-INF/libs/a.jar", "libs/b.jar"), SandboxPluginClassLoader.parseLibraries(props));
        assertEquals(List.of("libs/c.jar"), SandboxPluginClassLoader.parseRiskyLibraries(props));
    }

    // --- HTTP server ---

    @Test
    void permissionKeysAcceptHyphens() {
        assertEquals(PluginPermission.LIBRARIES, PluginPermission.fromKey("libraries"));
        assertEquals(PluginPermission.RISKY_LIBRARIES, PluginPermission.fromKey("risky-libraries"));
        assertEquals(PluginPermission.RISKY_LIBRARIES, PluginPermission.fromKey("risky_libraries"));
    }

    // --- Java applications ---

    @Test
    void httpServerNeedsTheNetworkPermission() throws Throwable {
        try (SandboxPluginClassLoader loader = loader(sandbox(PluginPermission.NETWORK))) {
            assertEquals("com.sun.net.httpserver.HttpServer", call(loader, "httpServer"));
            // It serves host directories
            assertThrows(SecurityException.class, () -> call(loader, "fileServer"));
        }
        try (SandboxPluginClassLoader loader = loader(sandbox())) {
            assertThrows(SecurityException.class, () -> call(loader, "httpServer"));
        }
    }

    @Test
    void applicationsNeedThePermission() throws IOException {
        PluginSandbox sandbox = sandbox();
        Path jar = exitApplication(sandbox);
        try (SandboxPluginClassLoader loader = loader(sandbox)) {
            assertThrows(SecurityException.class, () -> loader.loadApplication(jar, null));
        }
    }

    @Test
    void exitEndsTheApplication() throws Exception {
        PluginSandbox sandbox = sandbox(PluginPermission.JAVA_APPLICATIONS);
        Path jar = exitApplication(sandbox);
        try (SandboxPluginClassLoader loader = loader(sandbox);
             JavaApplication application = loader.loadApplication(jar, null)) {
            assertEquals("app.Main", application.getMainClass());
            assertEquals(2, application.run("a", "b"));
            assertEquals(1, application.start("a").get(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void runtimeExitEndsTheApplication() throws Exception {
        PluginSandbox sandbox = sandbox(PluginPermission.JAVA_APPLICATIONS);
        byte[] main = generate("app/Main", Map.of("main", main(mv -> {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;", false);
            mv.visitInsn(Opcodes.ICONST_3);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Runtime", "exit", "(I)V", false);
        })));
        Path jar = application(sandbox, "runtime.jar", Map.of("app/Main.class", main), Map.of());
        try (SandboxPluginClassLoader loader = loader(sandbox);
             JavaApplication application = loader.loadApplication(jar, "app.Main")) {
            assertEquals(3, application.run());
        }
    }

    @Test
    void applicationsAreSandboxed() throws Exception {
        PluginSandbox sandbox = sandbox(PluginPermission.JAVA_APPLICATIONS);
        byte[] main = generate("app/Main", Map.of("main", main(mv -> {
            getProperty(mv);
            mv.visitInsn(Opcodes.POP);
        })));
        Path jar = application(sandbox, "property.jar", Map.of("app/Main.class", main), Map.of("Main-Class", "app.Main"));
        try (SandboxPluginClassLoader loader = loader(sandbox);
             JavaApplication application = loader.loadApplication(jar, null)) {
            assertThrows(SecurityException.class, application::run);
        }
    }

    @Test
    void applicationsLoadTheirClassPath() throws Exception {
        PluginSandbox sandbox = sandbox(PluginPermission.JAVA_APPLICATIONS);
        byte[] lib = generate("applib/Lib", Map.of("value", Map.entry("()I", mv -> {
            mv.visitIntInsn(Opcodes.BIPUSH, 7);
            mv.visitInsn(Opcodes.IRETURN);
        })));
        application(sandbox, "lib/applib.jar", Map.of("applib/Lib.class", lib), Map.of());
        byte[] main = generate("app/Main", Map.of("main", main(mv -> {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "applib/Lib", "value", "()I", false);
            exit(mv);
        })));
        Path jar = application(sandbox, "classpath.jar", Map.of("app/Main.class", main),
                Map.of("Main-Class", "app.Main", "Class-Path", "lib/applib.jar ../../outside.jar"));
        try (SandboxPluginClassLoader loader = loader(sandbox);
             JavaApplication application = loader.loadApplication(jar, null)) {
            assertEquals(7, application.run());
        }
    }

    @Test
    void applicationsAreIsolatedFromThePlugin() throws Exception {
        PluginSandbox sandbox = sandbox(PluginPermission.JAVA_APPLICATIONS, PluginPermission.LIBRARIES);
        Path jar = exitApplication(sandbox);
        try (SandboxPluginClassLoader loader = loader(sandbox);
             SandboxJavaApplication application = loader.loadApplication(jar, null)) {
            Class<?> main = Class.forName("app.Main", false, appLoader(application));
            SandboxPluginClassLoader appLoader = (SandboxPluginClassLoader) main.getClassLoader();
            assertNotSame(loader, appLoader);
            assertSame(loader, appLoader.pluginLoader());
            assertSame(sandbox, appLoader.sandbox());
            assertThrows(ClassNotFoundException.class, () -> appLoader.loadClass("testplugin.Plugin"));
            assertThrows(ClassNotFoundException.class, () -> loader.loadClass("app.Main"));
        }
    }

    @Test
    void onlyTheApplicationsClassesCanBeRun() throws IOException {
        PluginSandbox sandbox = sandbox(PluginPermission.JAVA_APPLICATIONS);
        Path jar = exitApplication(sandbox);
        try (SandboxPluginClassLoader loader = loader(sandbox);
             JavaApplication other = loader.loadApplication(jar, "java.lang.Object");
             JavaApplication none = loader.loadApplication(application(sandbox, "empty.jar", Map.of(), Map.of()), null)) {
            assertThrows(ClassNotFoundException.class, other::run);
            assertNull(none.getMainClass());
            assertThrows(IllegalStateException.class, none::run);
        }
    }

    @Test
    void unloadingThePluginClosesItsApplications() throws IOException {
        PluginSandbox sandbox = sandbox(PluginPermission.JAVA_APPLICATIONS);
        Path jar = exitApplication(sandbox);
        SandboxPluginClassLoader loader = loader(sandbox);
        JavaApplication application = loader.loadApplication(jar, null);
        loader.close();
        assertTrue(application.isClosed());
        assertThrows(IllegalStateException.class, application::run);
        assertThrows(IllegalStateException.class, () -> loader.loadApplication(jar, null));
    }

    @Test
    void missingApplicationsFailLoading() throws IOException {
        PluginSandbox sandbox = sandbox(PluginPermission.JAVA_APPLICATIONS);
        try (SandboxPluginClassLoader loader = loader(sandbox)) {
            assertThrows(IOException.class, () -> loader.loadApplication(sandbox.getDataFolder().resolve("missing.jar"), null));
        }
    }
}
