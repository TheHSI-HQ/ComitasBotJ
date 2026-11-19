package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Invite;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.Compatibility.CompatibilityPlan;
import org.jetbrains.annotations.Nullable;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A plugin, whose Discord permissions are limited, loaded through the sandbox class loader.
 */
class SandboxDiscordPermissionTest {
    private static final String MEMBER = "cloud/thehsi/ComitasBotJ/API/Discord/User/Member";
    private static final String BAN = "cloud/thehsi/ComitasBotJ/API/Discord/Guild/Ban";
    private static final String GUILD = "cloud/thehsi/ComitasBotJ/API/Discord/Guild/Guild";
    private static final String INVITE = "cloud/thehsi/ComitasBotJ/API/Discord/Guild/Invite";
    private static final String TEXT_CHANNEL = "cloud/thehsi/ComitasBotJ/API/Discord/Channel/TextChannel";
    private static final String COMPONENT = "cloud/thehsi/ComitasBotJ/API/Discord/Message/Components/Component";
    private static final String MY_MESSAGE = "cloud/thehsi/ComitasBotJ/API/Discord/Message/MyMessage";

    @TempDir
    Path temp;

    @Test
    void limitedPluginCanOnlyCallGrantedMethods() throws Exception {
        try (SandboxPluginClassLoader loader = loader(EnumSet.of(Permission.SEND_MESSAGES))) {
            Class<?> plugin = loader.loadClass("demo.DiscordPlugin");

            // Inherited from MessageChannel, needs SEND_MESSAGES
            assertNull(invoke(plugin, "send", proxy(TextChannel.class), null));
            // Not annotated
            assertEquals("guild", invoke(plugin, "name", proxy(Guild.class, Map.of("getName", "guild"))));

            SandboxViolationException ban = assertDenied(plugin, "ban", proxy(Member.class));
            assertTrue(ban.getMessage().contains("[ban_members]"), ban.getMessage());

            SandboxViolationException delete = assertDenied(plugin, "deleteInvite", proxy(Invite.class));
            assertTrue(delete.getMessage().contains("manage_channel") && delete.getMessage().contains("manage_guild"),
                    delete.getMessage());
        }
    }

    @Test
    void oneAlternativeIsEnough() throws Exception {
        try (SandboxPluginClassLoader loader = loader(EnumSet.of(Permission.MANAGE_GUILD))) {
            assertNull(invoke(loader.loadClass("demo.DiscordPlugin"), "deleteInvite", proxy(Invite.class)));
        }
    }

    @Test
    void sendingInThreadsPermitsSending() throws Exception {
        try (SandboxPluginClassLoader loader = loader(EnumSet.of(Permission.SEND_MESSAGES_IN_THREADS))) {
            assertNull(invoke(loader.loadClass("demo.DiscordPlugin"), "send", proxy(TextChannel.class), null));
        }
    }

    @Test
    void unlimitedAndAdministratorPluginsCanCallEverything() throws Exception {
        try (SandboxPluginClassLoader loader = loader(EnumSet.of(Permission.ADMINISTRATOR))) {
            assertNull(invoke(loader.loadClass("demo.DiscordPlugin"), "ban", proxy(Member.class)));
        }
        try (SandboxPluginClassLoader loader = loader(null)) {
            assertNull(invoke(loader.loadClass("demo.DiscordPlugin"), "ban", proxy(Member.class)));
        }
    }

    @Test
    void contentPermissionsAreCheckedForTheCallingPlugin() throws Exception {
        MessageData withEmbed = new MessageData(proxy(Embed.class));
        Runnable check = () -> PluginDiscordPermissions.requireForContent(withEmbed);

        try (SandboxPluginClassLoader loader = loader(EnumSet.of(Permission.SEND_MESSAGES))) {
            SandboxViolationException e = assertDenied(loader.loadClass("demo.DiscordPlugin"), "run", check);
            assertTrue(e.getMessage().contains("[embed_links]"), e.getMessage());
        }
        try (SandboxPluginClassLoader loader = loader(EnumSet.of(Permission.SEND_MESSAGES, Permission.EMBED_LINKS))) {
            assertNull(invoke(loader.loadClass("demo.DiscordPlugin"), "run", check));
        }

        // Comitas itself (no plugin on the stack) isn't limited
        assertDoesNotThrow(check::run);
    }

    private SandboxViolationException assertDenied(Class<?> plugin, String method, Object... args) {
        InvocationTargetException e = assertThrows(InvocationTargetException.class, () -> invoke(plugin, method, args));
        return assertInstanceOf(SandboxViolationException.class, e.getCause());
    }

    private static Object invoke(Class<?> type, String name, Object... args) throws Exception {
        for (Method method : type.getMethods())
            if (method.getName().equals(name) && method.getParameterCount() == args.length) return method.invoke(null, args);
        throw new NoSuchMethodException(name);
    }

    private SandboxPluginClassLoader loader(@Nullable Set<Permission> discordGranted) throws IOException {
        PluginSandbox sandbox = new PluginSandbox("DiscordPlugin", UUID.randomUUID(), Set.of(), Map.of(),
                Files.createDirectories(temp.resolve("data")), discordGranted, List.of());
        return new SandboxPluginClassLoader(pluginJar(), sandbox, List.of(), (CompatibilityPlan) null);
    }

    private File pluginJar() throws IOException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "demo/DiscordPlugin", null, "java/lang/Object", null);

        method(cw, "ban", "(L" + MEMBER + ";)L" + BAN + ";", mv -> {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, MEMBER, "ban", "()L" + BAN + ";", true);
            mv.visitInsn(Opcodes.ARETURN);
        });
        method(cw, "send", "(L" + TEXT_CHANNEL + ";L" + COMPONENT + ";)L" + MY_MESSAGE + ";", mv -> {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, TEXT_CHANNEL, "sendMessage", "(L" + COMPONENT + ";)L" + MY_MESSAGE + ";", true);
            mv.visitInsn(Opcodes.ARETURN);
        });
        method(cw, "deleteInvite", "(L" + INVITE + ";)Ljava/lang/Object;", mv -> {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, INVITE, "delete", "()V", true);
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
        });
        method(cw, "name", "(L" + GUILD + ";)Ljava/lang/String;", mv -> {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, GUILD, "getName", "()Ljava/lang/String;", true);
            mv.visitInsn(Opcodes.ARETURN);
        });
        method(cw, "run", "(Ljava/lang/Runnable;)Ljava/lang/Object;", mv -> {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/lang/Runnable", "run", "()V", true);
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
        });
        cw.visitEnd();

        Path jar = temp.resolve("discord-plugin.jar");
        try (OutputStream out = Files.newOutputStream(jar); JarOutputStream jarOut = new JarOutputStream(out)) {
            jarOut.putNextEntry(new JarEntry("demo/DiscordPlugin.class"));
            jarOut.write(cw.toByteArray());
            jarOut.closeEntry();
        }
        return jar.toFile();
    }

    private static void method(ClassWriter cw, String name, String descriptor, java.util.function.Consumer<MethodVisitor> body) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, descriptor, null, null);
        mv.visitCode();
        body.accept(mv);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static <T> T proxy(Class<T> type) {
        return proxy(type, Map.of());
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
