package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MissingBotPermissionException;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.Compatibility.CompatibilityPlan;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JDA exceptions thrown by API calls reach plugins as API exceptions.
 */
class SandboxApiExceptionTest {
    private static final String SOURCE = """
            package demo;

            import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
            import cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MissingBotPermissionException;
            import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
            import java.util.function.BiFunction;
            import java.util.function.Function;

            public class Sender {
                public static Object send(TextChannel channel) {
                    return channel.sendMessage((Component) null);
                }

                public static String sendCaught(TextChannel channel) {
                    try {
                        channel.sendMessage((Component) null);
                        return "sent";
                    } catch (MissingBotPermissionException e) {
                        return e.getPermission().name();
                    }
                }

                public static Object sendReference(TextChannel channel) {
                    Function<Component, ?> send = channel::sendMessage;
                    return send.apply(null);
                }

                public static Object sendUnbound(TextChannel channel) {
                    BiFunction<TextChannel, Component, ?> send = TextChannel::sendMessage;
                    return send.apply(channel, null);
                }
            }
            """;

    @TempDir
    Path temp;

    @Test
    void insufficientPermissionsBecomeMissingBotPermission() throws Exception {
        try (SandboxPluginClassLoader loader = loader()) {
            Class<?> sender = loader.loadClass("demo.Sender");
            TextChannel channel = channel(() -> new InsufficientPermissionException(
                    guild(), net.dv8tion.jda.api.Permission.MESSAGE_SEND));

            MissingBotPermissionException direct = assertMissing(sender, "send", channel);
            assertEquals(Permission.SEND_MESSAGES, direct.getPermission());
            assertEquals("The bot is missing the SEND_MESSAGES permission in guild 42", direct.getMessage());
            assertInstanceOf(InsufficientPermissionException.class, direct.getCause());

            // The plugin's own handler sees the translated exception
            assertEquals("SEND_MESSAGES", invoke(sender, "sendCaught", channel));
            // Method references are called by the JDK, not the plugin's code
            assertEquals(Permission.SEND_MESSAGES, assertMissing(sender, "sendReference", channel).getPermission());
            assertEquals(Permission.SEND_MESSAGES, assertMissing(sender, "sendUnbound", channel).getPermission());
        }
    }

    @Test
    void otherExceptionsAreUnchanged() throws Exception {
        IllegalStateException original = new IllegalStateException("unrelated");
        try (SandboxPluginClassLoader loader = loader()) {
            Class<?> sender = loader.loadClass("demo.Sender");
            InvocationTargetException e = assertThrows(InvocationTargetException.class,
                    () -> invoke(sender, "send", channel(() -> original)));
            assertSame(original, e.getCause());
        }
    }

    @Test
    void successfulCallsStillReturn() throws Exception {
        try (SandboxPluginClassLoader loader = loader()) {
            assertEquals("sent", invoke(loader.loadClass("demo.Sender"), "sendCaught", channel(() -> null)));
        }
    }

    private MissingBotPermissionException assertMissing(Class<?> sender, String method, TextChannel channel) {
        InvocationTargetException e = assertThrows(InvocationTargetException.class, () -> invoke(sender, method, channel));
        return assertInstanceOf(MissingBotPermissionException.class, e.getCause());
    }

    /**
     * @param failure The exception sendMessage throws, or null to succeed
     */
    private static TextChannel channel(@NotNull Supplier<RuntimeException> failure) {
        return (TextChannel) Proxy.newProxyInstance(TextChannel.class.getClassLoader(), new Class<?>[]{TextChannel.class},
                (self, method, args) -> {
                    if (method.getName().equals("sendMessage")) {
                        RuntimeException e = failure.get();
                        if (e != null) throw e;
                    }
                    //noinspection SuspiciousInvocationHandlerImplementation
                    return null;
                });
    }

    private static Guild guild() {
        //noinspection SuspiciousInvocationHandlerImplementation
        return (Guild) Proxy.newProxyInstance(Guild.class.getClassLoader(), new Class<?>[]{Guild.class},
                (self, method, args) -> method.getName().equals("getIdLong") ? 42L : null);
    }

    private static Object invoke(Class<?> type, String name, Object... args) throws Exception {
        for (Method method : type.getMethods())
            if (method.getName().equals(name) && method.getParameterCount() == args.length) return method.invoke(null, args);
        throw new NoSuchMethodException(name);
    }

    private SandboxPluginClassLoader loader() throws Exception {
        PluginSandbox sandbox = new PluginSandbox("Sender", UUID.randomUUID(), Set.of(), Map.of(),
                Files.createDirectories(temp.resolve("data")));
        return new SandboxPluginClassLoader(compile(), sandbox, List.of(), (CompatibilityPlan) null);
    }

    private File compile() throws Exception {
        Path sources = Files.createDirectories(temp.resolve("src/demo"));
        Files.writeString(sources.resolve("Sender.java"), SOURCE);
        Path classes = Files.createDirectories(temp.resolve("classes"));

        String classpath = location(TextChannel.class) + File.pathSeparator + location(NotNull.class);
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        int exit = javac.run(null, errors, errors, "--release", "21", "-proc:none", "-d", classes.toString(),
                "-cp", classpath, sources.resolve("Sender.java").toString());
        assertEquals(0, exit, errors::toString);

        Path jar = temp.resolve("sender.jar");
        try (OutputStream out = Files.newOutputStream(jar); JarOutputStream jarOut = new JarOutputStream(out);
             Stream<Path> files = Files.walk(classes)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".class")).toList()) {
                jarOut.putNextEntry(new JarEntry(classes.relativize(file).toString().replace('\\', '/')));
                jarOut.write(Files.readAllBytes(file));
                jarOut.closeEntry();
            }
        }
        return jar.toFile();
    }

    private static String location(Class<?> type) throws Exception {
        return Path.of(type.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
    }
}
