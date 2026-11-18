package cloud.thehsi.ComitasBotJ.Compatibility;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.GuildChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.VoiceChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Builds "plugin" classes against the old API (with ASM, as javac would have compiled them),
 * runs them through the compatibility layer and executes them against the current API.
 */
class CompatibilityTransformerTest {
    private static final String GUILD = "cloud/thehsi/ComitasBotJ/API/Discord/Guild/Guild";
    private static final String TEXT_CHANNEL = "cloud/thehsi/ComitasBotJ/API/Discord/Channel/TextChannel";
    private static final String CONSOLE_COMMAND = "cloud/thehsi/ComitasBotJ/API/Console/ConsoleCommandRegistry$ConsoleCommand";
    private static final String EXECUTOR = "cloud/thehsi/ComitasBotJ/API/Console/ConsoleCommandExecutor";
    private static final String PLUGIN = "cloud/thehsi/ComitasBotJ/API/Plugin/Plugin";
    private static final String INVITE = "cloud/thehsi/ComitasBotJ/API/Discord/Guild/Invite";
    private static final String PERMISSION = "cloud/thehsi/ComitasBotJ/API/Discord/Permission";
    private static final String EXECUTE = "([Ljava/lang/String;Ljava/lang/String;L" + CONSOLE_COMMAND + ";)V";

    // --- bundled 0.0.17 changelog ---

    @Test
    void redirectsChangedReturnTypeToShim() throws Exception {
        CompatibilityReport report = new CompatibilityReport();
        Class<?> plugin = load(bundledTransformer(report), "test/DefaultChannel", cw -> {
            MethodVisitor mv = staticMethod(cw, "run", "(L" + GUILD + ";)Ljava/lang/Object;");
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, GUILD, "getDefaultChannel", "()L" + TEXT_CHANNEL + ";", true);
            mv.visitInsn(Opcodes.ARETURN);
            end(mv);
        });

        TextChannel text = proxy(TextChannel.class, Map.of());
        VoiceChannel voice = proxy(VoiceChannel.class, Map.of());
        Method run = plugin.getMethod("run", Guild.class);

        assertSame(text, run.invoke(null, proxy(Guild.class, Map.of("getDefaultChannel", text))));
        // The old API could only return text channels
        assertNull(run.invoke(null, proxy(Guild.class, Map.of("getDefaultChannel", (GuildChannel) voice))));
        assertEquals(List.of("guild-default-channel-type"), ids(report));
    }

    @Test
    void redirectsMethodReferenceToShim() throws Exception {
        CompatibilityReport report = new CompatibilityReport();
        Class<?> plugin = load(bundledTransformer(report), "test/DefaultChannelReference", cw -> {
            MethodVisitor mv = staticMethod(cw, "function", "()Ljava/util/function/Function;");
            Handle metafactory = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                    "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;"
                            + "Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)"
                            + "Ljava/lang/invoke/CallSite;", false);
            mv.visitInvokeDynamicInsn("apply", "()Ljava/util/function/Function;", metafactory,
                    Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;"),
                    new Handle(Opcodes.H_INVOKEINTERFACE, GUILD, "getDefaultChannel", "()L" + TEXT_CHANNEL + ";", true),
                    Type.getType("(L" + GUILD + ";)L" + TEXT_CHANNEL + ";"));
            mv.visitInsn(Opcodes.ARETURN);
            end(mv);
        });

        @SuppressWarnings("unchecked")
        Function<Guild, Object> function = (Function<Guild, Object>) plugin.getMethod("function").invoke(null);
        TextChannel text = proxy(TextChannel.class, Map.of());
        assertSame(text, function.apply(proxy(Guild.class, Map.of("getDefaultChannel", text))));
        assertEquals(List.of("guild-default-channel-type"), ids(report));
    }

    @Test
    void replacesConstructorAndAccessor() throws Exception {
        CompatibilityReport report = new CompatibilityReport();
        String oldConstructor = "([Ljava/lang/String;L" + PLUGIN + ";Ljava/lang/String;L" + EXECUTOR + ";)V";
        Class<?> plugin = load(bundledTransformer(report), "test/ConsoleCommands", cw -> {
            MethodVisitor mv = staticMethod(cw, "aliases", "([Ljava/lang/String;)[Ljava/lang/String;");
            mv.visitTypeInsn(Opcodes.NEW, CONSOLE_COMMAND);
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitLdcInsn("description");
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CONSOLE_COMMAND, "<init>", oldConstructor, false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, CONSOLE_COMMAND, "aliases", "()[Ljava/lang/String;", false);
            mv.visitInsn(Opcodes.ARETURN);
            end(mv);
        });

        String[] aliases = {"a", "b"};
        assertArrayEquals(aliases, (String[]) plugin.getMethod("aliases", String[].class).invoke(null, (Object) aliases));
        assertEquals(List.of("console-command-aliases-list", "console-command-aliases-list-constructor"), ids(report));
    }

    @Test
    void reportsNotesWithoutChangingCode() {
        CompatibilityReport report = new CompatibilityReport();
        byte[] original = build("test/Notes", cw -> {
            MethodVisitor mv = staticMethod(cw, "run", "(L" + INVITE + ";)Ljava/lang/Object;");
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, INVITE, "getExpiryTime", "()Ljava/time/OffsetDateTime;", true);
            mv.visitInsn(Opcodes.POP);
            mv.visitFieldInsn(Opcodes.GETSTATIC, PERMISSION, "PIN_MESSAGES", "L" + PERMISSION + ";");
            mv.visitInsn(Opcodes.ARETURN);
            end(mv);
        });

        assertSame(original, bundledTransformer(report).transform(original));
        assertEquals(List.of("invite-expiry-nullable"), ids(report));
    }

    // --- renames, with a made up changelog ---

    private static final String RENAMES = """
            {
              "schema": 1, "from": "0.0.1", "to": "0.0.2",
              "changes": [
                {"id": "class", "kind": "CLASS_RENAMED", "class": "cloud.thehsi.ComitasBotJ.API.Discord.Guild.Server",
                 "newName": "cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild", "description": "Server is Guild now"},
                {"id": "method", "kind": "METHOD_RENAMED", "class": "cloud.thehsi.ComitasBotJ.API.Discord.Guild.Server",
                 "name": "getTitle", "descriptor": "()Ljava/lang/String;", "newName": "getName", "description": "d"},
                {"id": "inherited", "kind": "METHOD_RENAMED", "class": "cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel",
                 "name": "getTitle", "descriptor": "()Ljava/lang/String;", "newName": "getName", "description": "d"},
                {"id": "field", "kind": "FIELD_RENAMED", "class": "cloud.thehsi.ComitasBotJ.API.Discord.Permission",
                 "name": "ADMIN", "descriptor": "Lcloud/thehsi/ComitasBotJ/API/Discord/Permission;", "static": true,
                 "newName": "ADMINISTRATOR", "description": "d"},
                {"id": "override", "kind": "METHOD_RENAMED", "class": "cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor",
                 "name": "run", "descriptor": "([Ljava/lang/String;Ljava/lang/String;Lcloud/thehsi/ComitasBotJ/API/Console/ConsoleCommandRegistry$ConsoleCommand;)V", "newName": "execute", "description": "d"}
              ]
            }
            """;

    @Test
    void renamesClassesMethodsAndFields() throws Exception {
        CompatibilityReport report = new CompatibilityReport();
        String server = "cloud/thehsi/ComitasBotJ/API/Discord/Guild/Server";
        Class<?> plugin = load(renameTransformer(report), "test/Renames", cw -> {
            MethodVisitor mv = staticMethod(cw, "title", "(Ljava/lang/Object;)Ljava/lang/String;");
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitTypeInsn(Opcodes.CHECKCAST, server);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, server, "getTitle", "()Ljava/lang/String;", true);
            mv.visitInsn(Opcodes.ARETURN);
            end(mv);

            // getTitle() is declared in Channel, called through TextChannel
            mv = staticMethod(cw, "channelTitle", "(L" + TEXT_CHANNEL + ";)Ljava/lang/String;");
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, TEXT_CHANNEL, "getTitle", "()Ljava/lang/String;", true);
            mv.visitInsn(Opcodes.ARETURN);
            end(mv);

            mv = staticMethod(cw, "admin", "()Ljava/lang/Object;");
            mv.visitFieldInsn(Opcodes.GETSTATIC, PERMISSION, "ADMIN", "L" + PERMISSION + ";");
            mv.visitInsn(Opcodes.ARETURN);
            end(mv);
        });

        assertEquals("guild", plugin.getMethod("title", Object.class)
                .invoke(null, proxy(Guild.class, Map.of("getName", "guild"))));
        assertEquals("channel", plugin.getMethod("channelTitle", TextChannel.class)
                .invoke(null, proxy(TextChannel.class, Map.of("getName", "channel"))));
        assertSame(Permission.ADMINISTRATOR, plugin.getMethod("admin").invoke(null));
        assertEquals(List.of("class", "field", "inherited", "method"), ids(report));
    }

    @Test
    void renamesOverridingMethods() throws Exception {
        CompatibilityReport report = new CompatibilityReport();
        Class<?> plugin = load(renameTransformer(report), "test/Executor", EXECUTOR, cw -> {
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, EXECUTOR, "<init>", "()V", false);
            mv.visitInsn(Opcodes.RETURN);
            end(mv);

            // Stores the arguments in a static field, so the test can see the call
            cw.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "last", "[Ljava/lang/String;", null, null).visitEnd();
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "run", EXECUTE, null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, "test/Executor", "last", "[Ljava/lang/String;");
            mv.visitInsn(Opcodes.RETURN);
            end(mv);
        });

        ConsoleCommandExecutor executor = (ConsoleCommandExecutor) plugin.getConstructor().newInstance();
        String[] args = {"hello"};
        //noinspection DataFlowIssue
        executor.execute(args, "alias", null);
        assertSame(args, plugin.getField("last").get(null));
        assertEquals(List.of("override"), ids(report));
    }

    @Test
    void appliesChangelogsInOrder() {
        ApiChangelog first = ApiChangelog.parse(new StringReader("""
                {"schema": 1, "from": "0.0.1", "to": "0.0.2", "changes": [
                  {"id": "a", "kind": "CLASS_RENAMED", "class": "cloud.thehsi.ComitasBotJ.API.A",
                   "newName": "cloud.thehsi.ComitasBotJ.API.B", "description": "d"}]}
                """), "first");
        ApiChangelog second = ApiChangelog.parse(new StringReader("""
                {"schema": 1, "from": "0.0.2", "to": "0.0.3", "changes": [
                  {"id": "b", "kind": "CLASS_RENAMED", "class": "cloud.thehsi.ComitasBotJ.API.B",
                   "newName": "cloud.thehsi.ComitasBotJ.API.C", "description": "d"}]}
                """), "second");
        ChangelogRepository repository = new ChangelogRepository(List.of(second, first));

        CompatibilityPlan fromOldest = repository.plan("0.0.1", "0.0.3");
        assertNotNull(fromOldest);
        assertEquals("cloud/thehsi/ComitasBotJ/API/C", fromOldest.mapClassName("cloud/thehsi/ComitasBotJ/API/A"));
        assertEquals("cloud/thehsi/ComitasBotJ/API/C$Inner", fromOldest.mapClassName("cloud/thehsi/ComitasBotJ/API/A$Inner"));

        CompatibilityPlan fromMiddle = repository.plan("0.0.2", "0.0.3");
        assertNotNull(fromMiddle);
        assertEquals(List.of("b"), fromMiddle.changes().stream().map(Change::id).toList());

        assertNull(repository.plan("0.0.0", "0.0.3"), "Older than every changelog");
        assertNull(repository.plan("0.0.3", "0.0.3"), "Already current");
    }

    @Test
    void rejectsChangesOutsideTheApi() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> ApiChangelog.parse(new StringReader("""
                {"schema": 1, "from": "0.0.1", "to": "0.0.2", "changes": [
                  {"id": "a", "kind": "METHOD_REPLACED", "class": "cloud.thehsi.ComitasBotJ.API.A", "name": "x",
                   "descriptor": "()V", "shim": "cloud.thehsi.ComitasBotJ.Sandbox.SandboxRuntime#x", "description": "d"}]}
                """), "test"));
        assertTrue(e.getMessage().contains("shims must be in"), e.getMessage());

        e = assertThrows(IllegalArgumentException.class, () -> ApiChangelog.parse(new StringReader("""
                {"schema": 1, "from": "0.0.1", "to": "0.0.2", "changes": [
                  {"id": "a", "kind": "CLASS_RENAMED", "class": "java.lang.String",
                   "newName": "cloud.thehsi.ComitasBotJ.API.B", "description": "d"}]}
                """), "test"));
        assertTrue(e.getMessage().contains("must be in"), e.getMessage());
    }

    // --- helpers ---

    private static CompatibilityTransformer bundledTransformer(CompatibilityReport report) {
        CompatibilityPlan plan = ChangelogRepository.bundled().plan("0.0.16", "0.0.17");
        assertNotNull(plan);
        return new CompatibilityTransformer(plan, TypeResolver.host(CompatibilityTransformerTest.class.getClassLoader()), report);
    }

    private static CompatibilityTransformer renameTransformer(CompatibilityReport report) {
        ChangelogRepository repository = new ChangelogRepository(List.of(ApiChangelog.parse(new StringReader(RENAMES), "renames")));
        CompatibilityPlan plan = repository.plan("0.0.1", "0.0.2");
        assertNotNull(plan);
        return new CompatibilityTransformer(plan, TypeResolver.host(CompatibilityTransformerTest.class.getClassLoader()), report);
    }

    private static List<String> ids(CompatibilityReport report) {
        return report.changes().stream().map(Change::id).toList();
    }

    private static Class<?> load(CompatibilityTransformer transformer, String name, Consumer<ClassWriter> body) {
        return load(transformer, name, "java/lang/Object", body);
    }

    private static Class<?> load(CompatibilityTransformer transformer, String name, String superName, Consumer<ClassWriter> body) {
        byte[] transformed = transformer.transform(build(name, superName, body));
        return new DefiningLoader().define(name.replace('/', '.'), transformed);
    }

    private static byte[] build(@SuppressWarnings("SameParameterValue") String name, Consumer<ClassWriter> body) {
        return build(name, "java/lang/Object", body);
    }

    private static byte[] build(String name, String superName, Consumer<ClassWriter> body) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, name, null, superName, null);
        body.accept(cw);
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static MethodVisitor staticMethod(ClassWriter cw, String name, String descriptor) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, descriptor, null, null);
        mv.visitCode();
        return mv;
    }

    private static void end(MethodVisitor mv) {
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * An API object, whose methods return the given values (by method name).
     */
    private static <T> T proxy(Class<T> type, Map<String, Object> values) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (self, method, args) -> switch (method.getName()) {
            case "toString" -> type.getSimpleName();
            case "hashCode" -> System.identityHashCode(self);
            case "equals" -> self == args[0];
            default -> values.get(method.getName());
        }));
    }

    private static final class DefiningLoader extends ClassLoader {
        DefiningLoader() {
            super(CompatibilityTransformerTest.class.getClassLoader());
        }

        Class<?> define(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
