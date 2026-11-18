package cloud.thehsi.ComitasBotJ.Compatibility;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ChannelType;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.VoiceChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Compiles plugin sources written against an old API (with javac, against the published old API jar),
 * runs them through the compatibility layer and executes them against the current API.
 */
class OldPluginEndToEndTest {
    @TempDir
    Path temp;

    @Test
    void pluginBuiltAgainst0016RunsOnCurrentApi() throws Exception {
        Map<String, byte[]> classes = compile("0.0.16");

        CompatibilityPlan plan = ChangelogRepository.bundled().plan("0.0.16", System.getProperty("comitas.version"));
        assertNotNull(plan);
        CompatibilityReport report = new CompatibilityReport();
        CompatibilityTransformer transformer = new CompatibilityTransformer(plan,
                TypeResolver.host(getClass().getClassLoader()), report);

        Map<String, byte[]> transformed = new HashMap<>();
        classes.forEach((name, bytes) -> transformed.put(name, transformer.transform(bytes)));
        Class<?> plugin = new MapLoader(transformed).loadClass("demo.OldPlugin");

        // Guild#getDefaultChannel() returned TextChannel
        TextChannel text = proxy(TextChannel.class, Map.of());
        Guild guild = proxy(Guild.class, Map.of("getDefaultChannel", text));
        assertSame(text, invoke(plugin, "defaultChannel", guild));
        assertNull(invoke(plugin, "defaultChannel", proxy(Guild.class, Map.of("getDefaultChannel", proxy(VoiceChannel.class, Map.of())))));

        @SuppressWarnings("unchecked")
        Function<Guild, TextChannel> function = (Function<Guild, TextChannel>) invoke(plugin, "defaultChannelFunction");
        assertSame(text, function.apply(guild));

        // Reaction#getReactors() returned members
        Member member = proxy(Member.class, Map.of());
        assertEquals(2, invoke(plugin, "reactorCount",
                proxy(Reaction.class, Map.of("getReactorMembers", List.of(member, member)))));

        // ConsoleCommand took / returned String[]
        assertArrayEquals(new String[]{"a", "b"}, (String[]) invoke(plugin, "aliases", null, new String[]{"a", "b"}));

        assertSame(CommandArgumentType.NUMBER, invoke(plugin, "integerType"));
        assertEquals("voice", invoke(plugin, "describe", ChannelType.VOICE));
        assertEquals("text", invoke(plugin, "describe", ChannelType.TEXT));

        // ConsoleCommandExecutor#execute(String[]) got alias and command parameters, the plugin gets a bridge
        ConsoleCommandExecutor executor = (ConsoleCommandExecutor) invoke(plugin, "executor");
        String[] args = {"x"};
        //noinspection DataFlowIssue
        executor.execute(args, "alias", null);
        assertSame(args, plugin.getField("lastArgs").get(null));

        List<String> ids = report.changes().stream().map(Change::id).toList();
        assertEquals(List.of(
                "channel-type-voice-channel",
                "command-argument-type-integer-renamed",
                "command-argument-type-number-values",
                "console-command-aliases-list",
                "console-command-aliases-list-constructor",
                "console-command-executor-alias-and-command",
                "guild-default-channel-type",
                "reaction-reactors-are-users"), ids);
    }

    @Test
    void pluginBuiltAgainst0017RunsOnCurrentApi() throws Exception {
        CompatibilityReport report = new CompatibilityReport();
        Class<?> plugin = load("0.0.17", "demo17.Plugin17", report);

        assertEquals("user,member,role", invoke(plugin, "names",
                proxy(User.class, Map.of("getConsoleLoggableName", "user")),
                proxy(Member.class, Map.of("getConsoleLoggableName", "member")),
                proxy(Role.class, Map.of("getConsoleLoggableName", "role"))));

        Member member = proxy(Member.class, Map.of());
        assertEquals(1, invoke(plugin, "memberCount", proxy(Reaction.class, Map.of("getReactorMembers", List.of(member)))));

        ConsoleCommandExecutor echo = (ConsoleCommandExecutor) plugin.getClassLoader().loadClass("demo17.Plugin17$Echo")
                .getConstructor().newInstance();
        ConsoleCommandRegistry.ConsoleCommand command = (ConsoleCommandRegistry.ConsoleCommand) invoke(plugin, "command", echo);
        assertEquals(List.of("a"), command.aliases());
        assertNull(command.consoleCommandTabCompleter());

        String[] args = {"y"};
        command.consoleCommandExecutor().execute(args, "a", command);
        assertSame(args, plugin.getField("lastArgs").get(null));

        assertEquals(List.of(
                "console-command-executor-alias-and-command",
                "console-command-tab-completer",
                // Member extends User, so the User rename already covers member.getLoggableName()
                "reaction-reactor-members-typo",
                "role-console-loggable-name",
                "user-console-loggable-name"), report.changes().stream().map(Change::id).toList());
    }

    private Class<?> load(@SuppressWarnings("SameParameterValue") String apiVersion, @SuppressWarnings("SameParameterValue") String className, CompatibilityReport report) throws Exception {
        Map<String, byte[]> classes = compile(apiVersion);
        CompatibilityPlan plan = ChangelogRepository.bundled().plan(apiVersion, System.getProperty("comitas.version"));
        assertNotNull(plan);
        CompatibilityTransformer transformer = new CompatibilityTransformer(plan,
                TypeResolver.host(getClass().getClassLoader()), report);
        Map<String, byte[]> transformed = new HashMap<>();
        classes.forEach((name, bytes) -> transformed.put(name, transformer.transform(bytes)));
        return new MapLoader(transformed).loadClass(className);
    }

    @NotNull
    private Map<String, byte[]> compile(@NotNull String apiVersion) throws Exception {
        Path out = Files.createDirectories(temp.resolve("classes-" + apiVersion));
        URL sources = getClass().getClassLoader().getResource("old-plugins/" + apiVersion);
        assertNotNull(sources);
        Path sourceRoot = Path.of(sources.toURI());
        List<String> files;
        try (Stream<Path> walk = Files.walk(sourceRoot)) {
            files = walk.map(Path::toString).filter(string -> string.endsWith(".java")).toList();
        }

        Path api = Path.of(System.getProperty("comitas.old.api.directory"), "comitas-api-" + apiVersion + ".jar");
        assertTrue(Files.isRegularFile(api), "Missing " + api);
        Path annotations = Path.of(NotNull.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        List<String> args = new java.util.ArrayList<>(List.of("--release", "21", "-proc:none", "-d", out.toString(),
                "-cp", api + java.io.File.pathSeparator + annotations));
        args.addAll(files);
        int exit = javac.run(null, errors, errors, args.toArray(String[]::new));
        assertEquals(0, exit, errors::toString);

        Map<String, byte[]> classes = new HashMap<>();
        try (Stream<Path> walk = Files.walk(out)) {
            for (Path file : walk.filter(f -> f.toString().endsWith(".class")).toList()) {
                String name = out.relativize(file).toString().replace('\\', '/').replace(".class", "").replace('/', '.');
                classes.put(name, Files.readAllBytes(file));
            }
        }
        return classes;
    }

    private static Object invoke(Class<?> type, String name, Object... args) throws Exception {
        for (Method method : type.getMethods())
            if (method.getName().equals(name) && method.getParameterCount() == args.length) return method.invoke(null, args);
        throw new NoSuchMethodException(name);
    }

    private static <T> T proxy(Class<T> type, Map<String, Object> values) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (self, method, args) -> switch (method.getName()) {
            case "hashCode" -> System.identityHashCode(self);
            case "equals" -> self == args[0];
            case "toString" -> type.getSimpleName();
            default -> values.get(method.getName());
        }));
    }

    private static final class MapLoader extends ClassLoader {
        private final Map<String, byte[]> classes;

        MapLoader(Map<String, byte[]> classes) {
            super(OldPluginEndToEndTest.class.getClassLoader());
            this.classes = classes;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = classes.get(name);
            if (bytes == null) throw new ClassNotFoundException(name);
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
