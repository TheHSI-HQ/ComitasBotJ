package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.Compatibility.Change;
import cloud.thehsi.ComitasBotJ.Compatibility.ChangelogRepository;
import cloud.thehsi.ComitasBotJ.Compatibility.CompatibilityPlan;
import cloud.thehsi.ComitasBotJ.Compatibility.CompatibilityReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A plugin built against API 0.0.16, loaded through the sandbox class loader in compatibility mode.
 */
class SandboxCompatibilityTest {
    private static final String GUILD = "cloud/thehsi/ComitasBotJ/API/Discord/Guild/Guild";
    private static final String TEXT_CHANNEL = "cloud/thehsi/ComitasBotJ/API/Discord/Channel/TextChannel";
    private static final String CONSOLE_COMMAND = "cloud/thehsi/ComitasBotJ/API/Console/ConsoleCommandRegistry$ConsoleCommand";
    private static final String OLD_CONSTRUCTOR = "([Ljava/lang/String;Lcloud/thehsi/ComitasBotJ/API/Plugin/Plugin;"
            + "Ljava/lang/String;Lcloud/thehsi/ComitasBotJ/API/Console/ConsoleCommandExecutor;)V";

    @TempDir
    Path temp;

    @Test
    void oldPluginRunsInCompatibilityMode() throws Exception {
        CompatibilityPlan plan = ChangelogRepository.bundled().plan("0.0.16",
                ChangelogRepository.bundled().changelogs().getLast().to());
        assertNotNull(plan);

        try (SandboxPluginClassLoader loader = new SandboxPluginClassLoader(oldPluginJar(), sandbox(), List.of(), plan)) {
            CompatibilityReport report = loader.scanCompatibility();
            assertNotNull(report);
            assertEquals(List.of("console-command-aliases-list", "console-command-aliases-list-constructor",
                    "guild-default-channel-type"), report.changes().stream().map(Change::id).toList());

            Class<?> plugin = loader.loadClass("demo.OldPlugin");

            TextChannel text = proxy(TextChannel.class, Map.of());
            Method defaultChannel = plugin.getMethod("defaultChannel", Guild.class);
            assertSame(text, defaultChannel.invoke(null, proxy(Guild.class, Map.of("getDefaultChannel", text))));

            Method aliases = plugin.getMethod("aliases", String[].class);
            String[] input = {"a", "b"};
            assertArrayEquals(input, (String[]) aliases.invoke(null, (Object) input));
        }
    }

    @Test
    void oldPluginFailsWithoutCompatibilityMode() throws Exception {
        try (SandboxPluginClassLoader loader = new SandboxPluginClassLoader(oldPluginJar(), sandbox(), List.of(), (CompatibilityPlan) null)) {
            assertNull(loader.scanCompatibility());
            Class<?> plugin = loader.loadClass("demo.OldPlugin");
            Method defaultChannel = plugin.getMethod("defaultChannel", Guild.class);
            Guild guild = proxy(Guild.class, Map.of());
            InvocationTargetException e = assertThrows(InvocationTargetException.class, () -> defaultChannel.invoke(null, guild));
            // The sandbox can't resolve the removed method and neutralizes the call
            assertInstanceOf(SandboxViolationException.class, e.getCause());
        }
    }

    private PluginSandbox sandbox() throws IOException {
        return new PluginSandbox("OldPlugin", UUID.randomUUID(), Set.of(), Map.of(),
                Files.createDirectories(temp.resolve("data")));
    }

    /**
     * What javac produced for a plugin built against 0.0.16.
     */
    private File oldPluginJar() throws IOException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "demo/OldPlugin", null, "java/lang/Object", null);

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "defaultChannel",
                "(L" + GUILD + ";)L" + TEXT_CHANNEL + ";", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, GUILD, "getDefaultChannel", "()L" + TEXT_CHANNEL + ";", true);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "aliases",
                "([Ljava/lang/String;)[Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitTypeInsn(Opcodes.NEW, CONSOLE_COMMAND);
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitLdcInsn("description");
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CONSOLE_COMMAND, "<init>", OLD_CONSTRUCTOR, false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CONSOLE_COMMAND, "aliases", "()[Ljava/lang/String;", false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();

        Path jar = temp.resolve("old-plugin.jar");
        try (OutputStream out = Files.newOutputStream(jar); JarOutputStream jarOut = new JarOutputStream(out)) {
            jarOut.putNextEntry(new JarEntry("demo/OldPlugin.class"));
            jarOut.write(cw.toByteArray());
            jarOut.closeEntry();
        }
        return jar.toFile();
    }

    private static <T> T proxy(Class<T> type, Map<String, Object> values) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (self, method, args) -> switch (method.getName()) {
            case "hashCode" -> System.identityHashCode(self);
            case "equals" -> self == args[0];
            case "toString" -> type.getSimpleName();
            default -> values.get(method.getName());
        }));
    }
}
