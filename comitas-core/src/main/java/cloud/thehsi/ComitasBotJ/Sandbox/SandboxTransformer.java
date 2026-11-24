package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginPermission;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Compatibility.CompatibilityTransformer;
import cloud.thehsi.ComitasBotJ.Discord.BotPermissionRequirements;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verifies and rewrites every class of a plugin, before it is defined.
 * <p>
 * Every reference to a class, field, method, method handle or bootstrap method is checked against
 * {@link SandboxPolicy}. Forbidden instructions are replaced by code throwing a
 * {@link SandboxViolationException}, so that a plugin still loads, if it e.g. contains an unused
 * code path of a shaded library, but can never execute it.
 * <p>
 * A few operations are redirected to {@link SandboxRuntime}, which checks them at runtime:
 * reflection (only on the plugin's own classes), {@code Class#getResourceAsStream}
 * (only the plugin's own jar), URL creation (only http/https) and {@code Path.of} / {@code Paths.get} /
 * {@code FileSystems.getDefault()} (the plugin's own file system).
 * <p>
 * Classes of the plugin's risky libraries ({@link RiskyLibraryClassLoader}) may be used like allowed host classes,
 * they only exist if the plugin was granted {@link PluginPermission#RISKY_LIBRARIES}.
 * <p>
 * The checks run on the original bytecode, the injected calls are not visible to plugins.
 */
final class SandboxTransformer {
    private static final String RUNTIME = "cloud/thehsi/ComitasBotJ/Sandbox/SandboxRuntime";
    private static final String CLASS = "java/lang/Class";
    private static final String URL = "java/net/URL";
    private static final String API_PACKAGE = "cloud/thehsi/ComitasBotJ/API/";

    /**
     * {@code Class} methods (name + descriptor), which are redirected to {@link SandboxRuntime}.
     */
    private static final Set<String> CLASS_REDIRECTS = Set.of(
            "getDeclaredFields()[Ljava/lang/reflect/Field;",
            "getFields()[Ljava/lang/reflect/Field;",
            "getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;",
            "getField(Ljava/lang/String;)Ljava/lang/reflect/Field;",
            "getDeclaredMethods()[Ljava/lang/reflect/Method;",
            "getMethods()[Ljava/lang/reflect/Method;",
            "getDeclaredMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
            "getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
            "getDeclaredConstructors()[Ljava/lang/reflect/Constructor;",
            "getConstructors()[Ljava/lang/reflect/Constructor;",
            "getDeclaredConstructor([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;",
            "getConstructor([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;",
            "getRecordComponents()[Ljava/lang/reflect/RecordComponent;",
            "getEnclosingMethod()Ljava/lang/reflect/Method;",
            "getEnclosingConstructor()Ljava/lang/reflect/Constructor;",
            "newInstance()Ljava/lang/Object;",
            "getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;"
    );

    private static final Set<String> URL_CONSTRUCTORS = Set.of(
            "(Ljava/lang/String;)V",
            "(Ljava/net/URL;Ljava/lang/String;)V",
            "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V",
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"
    );

    /**
     * Static file system lookups (owner.name + descriptor), redirected to the {@link SandboxRuntime} method,
     * so that they resolve in the plugin's own file system.
     */
    private static final Map<String, String> FILE_SYSTEM_REDIRECTS = Map.of(
            "java/nio/file/Path.of(Ljava/lang/String;[Ljava/lang/String;)Ljava/nio/file/Path;", "pathOf",
            "java/nio/file/Paths.get(Ljava/lang/String;[Ljava/lang/String;)Ljava/nio/file/Path;", "pathOf",
            "java/nio/file/FileSystems.getDefault()Ljava/nio/file/FileSystem;", "defaultFileSystem"
    );

    private final @NotNull PluginSandbox sandbox;
    private final @NotNull ClassHierarchy hierarchy;
    private final @Nullable CompatibilityTransformer compatibility;
    private final boolean application;
    private final @NotNull Map<String, Optional<String>> memberCache = new ConcurrentHashMap<>();

    SandboxTransformer(@NotNull PluginSandbox sandbox, @NotNull ClassHierarchy hierarchy) {
        this(sandbox, hierarchy, null);
    }

    /**
     * @param compatibility Rewrites old API usages before the checks, null if the plugin targets the current API
     */
    SandboxTransformer(@NotNull PluginSandbox sandbox, @NotNull ClassHierarchy hierarchy,
                       @Nullable CompatibilityTransformer compatibility) {
        this(sandbox, hierarchy, compatibility, false);
    }

    /**
     * @param application If the classes belong to a {@link SandboxJavaApplication}: {@code System.exit} then ends
     *                    the application's main method ({@link ApplicationExitError}) instead of being denied
     */
    SandboxTransformer(@NotNull PluginSandbox sandbox, @NotNull ClassHierarchy hierarchy,
                       @Nullable CompatibilityTransformer compatibility, boolean application) {
        this.sandbox = sandbox;
        this.hierarchy = hierarchy;
        this.compatibility = compatibility;
        this.application = application;
    }

    /**
     * @return The descriptor of the {@link SandboxRuntime#exit} variant, if the call exits the JVM, otherwise null
     */
    @Nullable
    private static String exitRedirect(@NotNull MethodInsnNode call) {
        if (!call.desc.equals("(I)V")) return null;
        if (call.getOpcode() == Opcodes.INVOKESTATIC && call.owner.equals("java/lang/System") && call.name.equals("exit"))
            return "(I)V";
        if (call.getOpcode() == Opcodes.INVOKEVIRTUAL && call.owner.equals("java/lang/Runtime")
                && (call.name.equals("exit") || call.name.equals("halt")))
            return "(Ljava/lang/Runtime;I)V";
        return null;
    }

    byte[] transform(@NotNull String internalName, byte[] original) {
        ClassNode node = new ClassNode();
        new ClassReader(original).accept(node, 0);

        if (!node.name.equals(internalName))
            throw new SandboxViolationException("Class file " + internalName + " declares a different class: " + node.name);

        boolean modified = false;
        if (compatibility != null) {
            CompatibilityTransformer.Result result = compatibility.transform(node);
            node = result.node();
            modified = result.modified();
        }

        checkStructure(node);
        List<MethodNode> bridges = new ArrayList<>();
        for (MethodNode method : node.methods)
            modified |= transformMethod(node, method, bridges);
        node.methods.addAll(bridges);

        if (!modified) return original;

        // Injected class constants need class file version 49+, frames are recomputed anyway
        if (node.version < Opcodes.V1_7) node.version = Opcodes.V1_7;

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                return hierarchy.commonSuperClass(type1, type2);
            }
        };
        node.accept(writer);
        return writer.toByteArray();
    }

    private void checkStructure(@NotNull ClassNode node) {
        List<String> supers = new ArrayList<>();
        if (node.superName != null) supers.add(node.superName);
        supers.addAll(node.interfaces);

        for (String type : supers) {
            String reason = checkType(type);
            if (reason != null)
                throw new SandboxViolationException("Plugin " + sandbox.pluginName() + ": class "
                        + node.name.replace('/', '.') + " may not extend / implement " + type.replace('/', '.')
                        + " (" + reason + ")");
        }
    }

    private boolean transformMethod(@NotNull ClassNode owner, @NotNull MethodNode method, @NotNull List<MethodNode> bridges) {
        InsnList instructions = method.instructions;
        boolean modified = false;
        Deque<AbstractInsnNode> pendingUrls = new ArrayDeque<>();
        List<MethodInsnNode> apiCalls = new ArrayList<>();

        for (AbstractInsnNode insn : instructions.toArray()) {
            String reason = null;

            switch (insn) {
                case MethodInsnNode call -> {
                    String exit = application ? exitRedirect(call) : null;
                    if (exit != null) {
                        instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, RUNTIME, "exit", exit, false));
                        modified = true;
                        continue;
                    }

                    if (call.owner.equals(CLASS) && call.getOpcode() == Opcodes.INVOKEVIRTUAL
                            && CLASS_REDIRECTS.contains(call.name + call.desc)) {
                        redirectClassMethod(owner, instructions, call);
                        modified = true;
                        continue;
                    }

                    String fileSystemRedirect = FILE_SYSTEM_REDIRECTS.get(call.owner + "." + call.name + call.desc);
                    if (fileSystemRedirect != null && call.getOpcode() == Opcodes.INVOKESTATIC) {
                        redirectToPluginFileSystem(owner, instructions, call, fileSystemRedirect);
                        modified = true;
                        continue;
                    }

                    if (call.owner.equals(URL) && call.name.equals("<init>")) {
                        AbstractInsnNode pendingNew = pendingUrls.poll();
                        if (pendingNew == null || !URL_CONSTRUCTORS.contains(call.desc)) {
                            reason = "java.net.URL can only be created from a String, use http(s) URLs";
                        } else if ((reason = permissionMissing(PluginPermission.NETWORK)) == null) {
                            instructions.remove(pendingNew.getNext()); // DUP
                            instructions.remove(pendingNew);           // NEW
                            String desc = call.desc.substring(0, call.desc.length() - 1) + "Ljava/net/URL;";
                            instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, RUNTIME, "newUrl", desc, false));
                            modified = true;
                            continue;
                        }
                        if (pendingNew != null)
                            neutralize(instructions, pendingNew, owner, method, reason);
                    } else if (call.owner.equals("java/net/URI") && call.name.equals("toURL")
                            && call.desc.equals("()Ljava/net/URL;")) {
                        if ((reason = permissionMissing(PluginPermission.NETWORK)) == null) {
                            instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, RUNTIME, "toUrl",
                                    "(Ljava/net/URI;)Ljava/net/URL;", false));
                            modified = true;
                            continue;
                        }
                    } else {
                        reason = checkMember(call.owner, call.name, call.desc, false);
                        if (reason == null && isApiMethod(call.owner, call.name)) apiCalls.add(call);
                    }
                }
                case FieldInsnNode field -> {
                    // System.out / err go to the plugin's logger, so plugins can't write raw escape sequences to the terminal
                    if (field.getOpcode() == Opcodes.GETSTATIC && field.owner.equals("java/lang/System")
                            && field.desc.equals("Ljava/io/PrintStream;")
                            && (field.name.equals("out") || field.name.equals("err"))) {
                        instructions.insertBefore(field, new LdcInsnNode(Type.getObjectType(owner.name)));
                        instructions.set(field, new MethodInsnNode(Opcodes.INVOKESTATIC, RUNTIME,
                                field.name.equals("out") ? "stdout" : "stderr",
                                "(Ljava/lang/Class;)Ljava/io/PrintStream;", false));
                        modified = true;
                        continue;
                    }
                    reason = checkMember(field.owner, field.name, field.desc, true);
                }
                case TypeInsnNode type -> {
                    if (type.getOpcode() == Opcodes.NEW && type.desc.equals(URL)) {
                        AbstractInsnNode next = type.getNext();
                        if (next != null && next.getOpcode() == Opcodes.DUP) {
                            pendingUrls.push(type);
                            continue;
                        }
                        reason = "java.net.URL can only be created from a String, use http(s) URLs";
                    } else {
                        reason = checkType(type.desc);
                    }
                }
                case MultiANewArrayInsnNode array -> reason = checkType(array.desc);
                case LdcInsnNode ldc -> reason = checkConstant(ldc.cst);
                case InvokeDynamicInsnNode indy -> {
                    reason = checkInvokeDynamic(indy);
                    if (reason == null && bridgeApiReferences(owner, indy, bridges)) modified = true;
                }
                default -> {
                }
            }

            if (reason != null) {
                neutralize(instructions, insn, owner, method, reason);
                modified = true;
            }
        }

        // NEW java/net/URL without a matching constructor call, never executed in valid javac output
        for (AbstractInsnNode leftover : pendingUrls) {
            neutralize(instructions, leftover, owner, method, "Unsupported java.net.URL construction");
            modified = true;
        }

        // Before the super constructor call, "this" is uninitialized, an exception handler there would need special
        // verification rules. API calls in constructors are rare, so they aren't translated.
        if (!apiCalls.isEmpty() && !method.name.equals("<init>")) {
            translateApiExceptions(method, apiCalls);
            modified = true;
        }

        return modified;
    }

    private static final String API_EXCEPTION_DESC = "(Ljava/lang/Throwable;)Ljava/lang/Throwable;";

    /**
     * @return Is the method part of the API (and not one of the plugin's own, e.g. inherited from {@code Plugin})
     */
    private boolean isApiMethod(@NotNull String owner, @NotNull String name) {
        return owner.startsWith(API_PACKAGE) && !hierarchy.isOwn(owner) && !name.equals("<init>");
    }

    /**
     * Wraps the API calls in an exception handler, which translates exceptions plugins can't handle,
     * like JDA's {@code InsufficientPermissionException}, into API exceptions
     * ({@link cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MissingBotPermissionException}), see {@link SandboxRuntime#apiException}.
     */
    private static void translateApiExceptions(@NotNull MethodNode method, @NotNull List<MethodInsnNode> calls) {
        List<TryCatchBlockNode> blocks = new ArrayList<>();
        for (MethodInsnNode call : calls) {
            LabelNode start = new LabelNode();
            LabelNode end = new LabelNode();
            LabelNode handler = new LabelNode();
            LabelNode after = new LabelNode();

            // The handler is placed right after the call, so it is covered by the same try blocks of the plugin
            // and the rethrown exception reaches them
            InsnList tail = new InsnList();
            tail.add(end);
            tail.add(new JumpInsnNode(Opcodes.GOTO, after));
            tail.add(handler);
            tail.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RUNTIME, "apiException", API_EXCEPTION_DESC, false));
            tail.add(new InsnNode(Opcodes.ATHROW));
            tail.add(after);

            method.instructions.insertBefore(call, start);
            method.instructions.insert(call, tail);
            blocks.add(new TryCatchBlockNode(start, end, handler, "java/lang/RuntimeException"));
        }
        // Innermost first, so they are found before the plugin's own handlers
        method.tryCatchBlocks.addAll(0, blocks);
    }

    /**
     * Method references to API methods ({@code channel::sendMessage}) are called by the JDK, not by the plugin's code.
     * They are redirected to a generated static method in the plugin's class, which calls the API method with the
     * exception handler of {@link #translateApiExceptions}.
     *
     * @return Was a reference redirected
     */
    private boolean bridgeApiReferences(@NotNull ClassNode owner, @NotNull InvokeDynamicInsnNode indy,
                                        @NotNull List<MethodNode> bridges) {
        if (!indy.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory")) return false;
        // Interfaces can only hold the (private static) bridge since Java 9
        boolean itf = (owner.access & Opcodes.ACC_INTERFACE) != 0;
        if (itf && (owner.version & 0xFFFF) < Opcodes.V9) return false;

        boolean redirected = false;
        for (int i = 0; i < indy.bsmArgs.length; i++) {
            if (!(indy.bsmArgs[i] instanceof Handle handle) || !isApiMethod(handle.getOwner(), handle.getName())) continue;
            int opcode = switch (handle.getTag()) {
                case Opcodes.H_INVOKEINTERFACE -> Opcodes.INVOKEINTERFACE;
                case Opcodes.H_INVOKEVIRTUAL -> Opcodes.INVOKEVIRTUAL;
                case Opcodes.H_INVOKESTATIC -> Opcodes.INVOKESTATIC;
                default -> -1;
            };
            if (opcode == -1) continue;

            Type target = Type.getMethodType(handle.getDesc());
            List<Type> parameters = new ArrayList<>();
            if (opcode != Opcodes.INVOKESTATIC) parameters.add(Type.getObjectType(handle.getOwner()));
            parameters.addAll(List.of(target.getArgumentTypes()));
            // Captured values (like the receiver of channel::sendMessage, which javac types as the variable's type,
            // e.g. TextChannel, while the handle names the declaring MessageChannel) must match a static target exactly
            Type[] captured = Type.getArgumentTypes(indy.desc);
            for (int c = 0; c < captured.length && c < parameters.size(); c++) parameters.set(c, captured[c]);
            String descriptor = Type.getMethodDescriptor(target.getReturnType(), parameters.toArray(Type[]::new));

            MethodNode bridge = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
                    "comitas$api$" + bridges.size(), descriptor, null, null);
            int slot = 0;
            for (Type parameter : parameters) {
                bridge.instructions.add(new VarInsnNode(parameter.getOpcode(Opcodes.ILOAD), slot));
                slot += parameter.getSize();
            }
            MethodInsnNode call = new MethodInsnNode(opcode, handle.getOwner(), handle.getName(), handle.getDesc(),
                    handle.isInterface());
            bridge.instructions.add(call);
            bridge.instructions.add(new InsnNode(target.getReturnType().getOpcode(Opcodes.IRETURN)));
            translateApiExceptions(bridge, List.of(call));
            bridges.add(bridge);

            indy.bsmArgs[i] = new Handle(Opcodes.H_INVOKESTATIC, owner.name, bridge.name, descriptor, itf);
            redirected = true;
        }
        return redirected;
    }

    private void redirectClassMethod(@NotNull ClassNode owner, @NotNull InsnList instructions, @NotNull MethodInsnNode call) {
        Type original = Type.getMethodType(call.desc);
        List<Type> args = new ArrayList<>();
        args.add(Type.getObjectType(CLASS));
        args.addAll(List.of(original.getArgumentTypes()));
        args.add(Type.getObjectType(CLASS));
        String desc = Type.getMethodDescriptor(original.getReturnType(), args.toArray(Type[]::new));

        instructions.insertBefore(call, new LdcInsnNode(Type.getObjectType(owner.name)));
        instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, RUNTIME, call.name, desc, false));
    }

    /**
     * Static file system lookups resolve inside the plugin's own file system, the caller is appended as a class constant.
     */
    private void redirectToPluginFileSystem(@NotNull ClassNode owner, @NotNull InsnList instructions,
                                            @NotNull MethodInsnNode call, @NotNull String target) {
        Type original = Type.getMethodType(call.desc);
        List<Type> args = new ArrayList<>(List.of(original.getArgumentTypes()));
        args.add(Type.getObjectType(CLASS));
        String desc = Type.getMethodDescriptor(original.getReturnType(), args.toArray(Type[]::new));

        instructions.insertBefore(call, new LdcInsnNode(Type.getObjectType(owner.name)));
        instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, RUNTIME, target, desc, false));
    }

    private void neutralize(@NotNull InsnList instructions, @NotNull AbstractInsnNode insn,
                            @NotNull ClassNode owner, @NotNull MethodNode method, @NotNull String reason) {
        String message = "Plugin " + sandbox.pluginName() + " (" + owner.name.replace('/', '.') + "#"
                + method.name + "): " + reason;
        if (DebugLogging.isBasicEnabled()) DebugLogging.getLogger().debug("Sandbox neutralized: {}", message);

        InsnList replacement = new InsnList();
        replacement.add(new LdcInsnNode(message));
        replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RUNTIME, "violation",
                "(Ljava/lang/String;)Ljava/lang/RuntimeException;", false));
        replacement.add(new InsnNode(Opcodes.ATHROW));
        instructions.insertBefore(insn, replacement);
        instructions.remove(insn);
    }

    @Nullable
    private String permissionMissing(@SuppressWarnings("SameParameterValue") @NotNull PluginPermission permission) {
        return sandbox.has(permission) ? null
                : "requires the " + permission.getKey() + " permission, which wasn't granted";
    }

    /**
     * @param type Internal name or descriptor of an array / object type
     * @return Null if the plugin may use the type, otherwise the reason why not
     */
    @Nullable
    String checkType(@NotNull String type) {
        if (type.startsWith("[")) {
            Type element = Type.getType(type).getElementType();
            if (element.getSort() != Type.OBJECT) return null;
            type = element.getInternalName();
        }

        if (hierarchy.isOwn(type) || hierarchy.isLibrary(type)) return null;

        return switch (SandboxPolicy.classAccess(type)) {
            case ALLOW -> null;
            case NETWORK -> permissionMissing(PluginPermission.NETWORK) == null
                    ? null : type.replace('/', '.') + " " + permissionMissing(PluginPermission.NETWORK);
            case JDBC -> sandbox.has(PluginPermission.NETWORK) || sandbox.has(PluginPermission.RISKY_LIBRARIES)
                    ? null : type.replace('/', '.') + " requires the network or risky_libraries permission, neither was granted";
            case DENY -> type.replace('/', '.') + " is not available to plugins";
        };
    }

    @Nullable
    private String checkDescriptorTypes(@NotNull String descriptor) {
        Type type = Type.getType(descriptor);
        List<Type> types = new ArrayList<>();
        if (type.getSort() == Type.METHOD) {
            types.addAll(List.of(type.getArgumentTypes()));
            types.add(type.getReturnType());
        } else {
            types.add(type);
        }

        for (Type t : types) {
            if (t.getSort() != Type.OBJECT && t.getSort() != Type.ARRAY) continue;
            String reason = checkType(t.getSort() == Type.ARRAY ? t.getDescriptor() : t.getInternalName());
            if (reason != null) return reason;
        }
        return null;
    }

    /**
     * @return Null if the plugin may use this field / method, otherwise the reason why not
     */
    @Nullable
    String checkMember(@NotNull String owner, @NotNull String name, @NotNull String descriptor, boolean field) {
        if (owner.startsWith("[")) return null; // clone() and Object methods on arrays

        String key = owner + "." + name + (field ? ":" : "") + descriptor;
        return memberCache.computeIfAbsent(key, k -> Optional.ofNullable(resolveMember(owner, name, descriptor, field)))
                .orElse(null);
    }

    @Nullable
    private String resolveMember(@NotNull String owner, @NotNull String name, @NotNull String descriptor, boolean field) {
        if (!hierarchy.isOwn(owner))
            return checkHostMember(owner, name, descriptor, field, true);

        // Walk the plugin's own hierarchy, until the member is found or the host classes are reached
        Deque<String> queue = new ArrayDeque<>(List.of(owner));
        Set<String> seen = new HashSet<>();
        List<String> hostSupers = new ArrayList<>();

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (!seen.add(current)) continue;

            if (!hierarchy.isOwn(current)) {
                hostSupers.add(current);
                continue;
            }

            ClassHierarchy.OwnClassInfo info = hierarchy.own(current);
            if (info == null) continue;
            if (field ? info.fields.contains(name + ":" + descriptor) : info.methods.contains(name + descriptor))
                return null;
            if (name.equals("<init>")) break; // constructors aren't inherited
            if (info.superName != null) queue.add(info.superName);
            queue.addAll(info.interfaces);
        }

        boolean found = false;
        for (String host : hostSupers) {
            String reason = checkHostMember(host, name, descriptor, field, false);
            if (reason == null) found = true;
            else if (!reason.equals(NOT_FOUND)) return reason;
        }

        return found ? null : owner.replace('/', '.') + "." + name + " could not be resolved";
    }

    private static final String NOT_FOUND = "\0not-found";

    @Nullable
    private String checkHostMember(@NotNull String owner, @NotNull String name, @NotNull String descriptor,
                                   boolean field, boolean direct) {
        String reason = checkType(owner);
        if (reason != null) return reason;

        Class<?> hostOwner = hierarchy.resolve(owner);
        if (hostOwner == null) return owner.replace('/', '.') + " could not be found";

        Class<?> declaring = ClassHierarchy.findDeclaring(hostOwner, name, descriptor, field);
        if (declaring == null)
            return direct ? owner.replace('/', '.') + "." + name + " could not be resolved" : NOT_FOUND;

        String declaringName = ClassHierarchy.internalName(declaring);
        if (!declaringName.equals(owner) && !SandboxPolicy.isInheritable(declaringName)
                && (reason = checkType(declaringName)) != null) return reason;

        if ((reason = SandboxPolicy.memberDenied(owner, name, descriptor)) != null) return reason;
        if ((reason = SandboxPolicy.memberDenied(declaringName, name, descriptor)) != null) return reason;
        if (!field && (reason = discordPermissionMissing(declaring, name, descriptor)) != null) return reason;

        return checkDescriptorTypes(descriptor);
    }

    /**
     * Checks the {@link cloud.thehsi.ComitasBotJ.API.Discord.RequiresBotPermission} of an API method against the
     * Discord permissions the plugin was granted. Conditional permissions are checked at runtime.
     *
     * @return Null if the plugin may call the method, otherwise the reason why not
     */
    @Nullable
    private String discordPermissionMissing(@NotNull Class<?> declaring, @NotNull String name, @NotNull String descriptor) {
        Set<Permission> allowed = sandbox.discordGranted();
        if (allowed == null || !ClassHierarchy.internalName(declaring).startsWith(API_PACKAGE)) return null;

        for (Method method : declaring.getDeclaredMethods()) {
            if (!method.getName().equals(name) || !Type.getMethodDescriptor(method).equals(descriptor)) continue;

            Set<Permission> missing = EnumSet.noneOf(Permission.class);
            for (BotPermissionRequirements.Requirement requirement : BotPermissionRequirements.of(method))
                missing.addAll(requirement.missingFrom(allowed));
            return missing.isEmpty() ? null : declaring.getSimpleName() + "#" + name + " requires the Discord permission(s) ["
                    + String.join(", ", missing.stream().map(PluginDiscordPermissions::key).toList())
                    + "], which weren't granted to the plugin";
        }
        return null;
    }

    @Nullable
    private String checkHandle(@NotNull Handle handle) {
        boolean field = handle.getTag() <= Opcodes.H_PUTSTATIC;
        return checkMember(handle.getOwner(), handle.getName(), handle.getDesc(), field);
    }

    @Nullable
    private String checkConstant(@Nullable Object constant) {
        return switch (constant) {
            case Type type when type.getSort() == Type.OBJECT -> checkType(type.getInternalName());
            case Type type when type.getSort() == Type.ARRAY -> checkType(type.getDescriptor());
            case Type ignored -> "method type constants are not available to plugins";
            case Handle ignored -> "method handles are not available to plugins";
            case ConstantDynamic ignored -> "dynamic constants are not available to plugins";
            case null, default -> null;
        };
    }

    @Nullable
    private String checkInvokeDynamic(@NotNull InvokeDynamicInsnNode indy) {
        Handle bootstrap = indy.bsm;
        if (!SandboxPolicy.isAllowedBootstrap(bootstrap.getOwner(), bootstrap.getName()))
            return "invokedynamic bootstrap " + bootstrap.getOwner().replace('/', '.') + "."
                    + bootstrap.getName() + " is not available to plugins";

        String reason = checkDescriptorTypes(indy.desc);
        if (reason != null) return reason;

        for (Object argument : indy.bsmArgs) {
            reason = switch (argument) {
                case Handle handle -> checkHandle(handle);
                case Type type when type.getSort() == Type.METHOD -> checkDescriptorTypes(type.getDescriptor());
                case Type type -> checkConstant(type);
                case ConstantDynamic ignored -> "dynamic constants are not available to plugins";
                default -> null;
            };
            if (reason != null) return reason;
        }

        return null;
    }
}
