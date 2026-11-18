package cloud.thehsi.ComitasBotJ.Compatibility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * Rewrites plugin classes built against an older API, so they link against the current API.
 * <p>
 * The changelogs of a {@link CompatibilityPlan} are applied one after another. For each changelog,
 * member changes are applied first (they refer to the old class names), then its class renames.
 * Redirected calls point to shims in {@value Change#SHIM_PACKAGE}, which only use the public API.
 * <p>
 * This only rewrites references, the plugin sandbox verifies the result like any other plugin class.
 */
public final class CompatibilityTransformer {
    private static final String SWITCH_BOOTSTRAPS = "java/lang/runtime/SwitchBootstraps";

    private final @NotNull List<Step> steps;
    private final @NotNull TypeResolver resolver;
    private final @NotNull CompatibilityReport report;

    public CompatibilityTransformer(@NotNull CompatibilityPlan plan, @NotNull TypeResolver resolver,
                                    @NotNull CompatibilityReport report) {
        this.steps = plan.steps().stream().map(Step::new).toList();
        this.resolver = resolver;
        this.report = report;
    }

    @NotNull
    public CompatibilityReport report() {
        return report;
    }

    /**
     * @param node The class, it may be modified
     * @return The transformed class (may be a different instance) and whether anything changed
     */
    @NotNull
    public Result transform(@NotNull ClassNode node) {
        boolean modified = false;
        for (Step step : steps) {
            modified |= applyMembers(step, node);
            if (!step.classRenames.isEmpty()) {
                RenamingRemapper remapper = new RenamingRemapper(step, node.name);
                ClassNode remapped = new ClassNode();
                node.accept(new ClassRemapper(remapped, remapper));
                if (remapper.changed) {
                    node = remapped;
                    modified = true;
                }
            }
        }
        return new Result(node, modified);
    }

    /**
     * Transforms a class file, stack map frames are recomputed if needed.
     *
     * @return The new class file, or the original if nothing changed
     */
    public byte @NotNull [] transform(byte @NotNull [] classFile) {
        ClassNode node = new ClassNode();
        new ClassReader(classFile).accept(node, 0);
        Result result = transform(node);
        if (!result.modified()) return classFile;

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                try {
                    return super.getCommonSuperClass(type1, type2);
                } catch (RuntimeException | LinkageError e) {
                    return "java/lang/Object";
                }
            }
        };
        result.node().accept(writer);
        return writer.toByteArray();
    }

    public record Result(@NotNull ClassNode node, boolean modified) {
    }

    // --- member changes ---

    private boolean applyMembers(@NotNull Step step, @NotNull ClassNode node) {
        boolean modified = false;

        for (Change note : step.classNotes)
            if (references(node, note.owner())) report.hit(note, node.name);

        // Plugin methods overriding a renamed API method are renamed as well
        for (MethodNode method : node.methods) {
            if ((method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE)) != 0 || method.name.startsWith("<")) continue;
            for (Change change : step.methods(method.name, method.desc)) {
                if (change.kind() != ChangeKind.METHOD_RENAMED || !inherits(node, change.owner())) continue;
                method.name = Objects.requireNonNull(change.newName());
                report.hit(change, node.name);
                modified = true;
                break;
            }
        }

        if ((node.access & Opcodes.ACC_INTERFACE) == 0)
            for (Change change : step.bridges)
                modified |= addBridge(change, node);

        for (MethodNode method : node.methods)
            modified |= applyToCode(step, node, method);

        return modified;
    }

    /**
     * A plugin class implements an abstract API method, whose signature changed: adds the new signature,
     * which calls the plugin's implementation of the old one.
     */
    private boolean addBridge(@NotNull Change change, @NotNull ClassNode node) {
        String name = Objects.requireNonNull(change.name());
        String oldDescriptor = Objects.requireNonNull(change.descriptor());
        String newDescriptor = Objects.requireNonNull(change.newDescriptor());
        boolean implementsOld = false;
        for (MethodNode method : node.methods) {
            if (!method.name.equals(name) || (method.access & Opcodes.ACC_STATIC) != 0) continue;
            if (method.desc.equals(newDescriptor)) return false; // already implements the new signature
            if (method.desc.equals(oldDescriptor) && (method.access & Opcodes.ACC_ABSTRACT) == 0) implementsOld = true;
        }
        if (!implementsOld || !inherits(node, change.owner())) return false;

        Type[] oldArgs = Type.getArgumentTypes(oldDescriptor);
        Type[] newArgs = Type.getArgumentTypes(newDescriptor);
        int[] slots = new int[newArgs.length];
        int slot = 1;
        for (int i = 0; i < newArgs.length; i++) {
            slots[i] = slot;
            slot += newArgs[i].getSize();
        }

        MethodNode bridge = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name, newDescriptor, null, null);
        InsnList code = bridge.instructions;
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        List<Integer> arguments = Objects.requireNonNull(change.arguments());
        for (int i = 0; i < oldArgs.length; i++) {
            int index = arguments.get(i);
            code.add(new VarInsnNode(newArgs[index].getOpcode(Opcodes.ILOAD), slots[index]));
        }
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name, name, oldDescriptor, false));
        code.add(new InsnNode(Type.getReturnType(oldDescriptor).getOpcode(Opcodes.IRETURN)));
        bridge.maxLocals = slot;
        bridge.maxStack = slot;
        node.methods.add(bridge);
        report.hit(change, node.name);
        return true;
    }

    private boolean applyToCode(@NotNull Step step, @NotNull ClassNode node, @NotNull MethodNode method) {
        InsnList instructions = method.instructions;
        boolean modified = false;
        // NEW instructions of types with replaced constructors, waiting for their constructor call
        Deque<TypeInsnNode> pendingNews = new ArrayDeque<>();

        for (AbstractInsnNode insn : instructions.toArray()) {
            switch (insn) {
                case TypeInsnNode type when type.getOpcode() == Opcodes.NEW && step.constructorOwners.contains(type.desc) ->
                        pendingNews.push(type);
                case MethodInsnNode call when call.name.equals("<init>") -> {
                    for (Change note : step.methods(call.name, call.desc))
                        if (note.kind() == ChangeKind.NOTE && note.owner().equals(call.owner)) report.hit(note, node.name);
                    if (!step.constructorOwners.contains(call.owner)) continue;
                    TypeInsnNode pendingNew = popNew(pendingNews, call.owner);
                    Change change = find(step.methods(call.name, call.desc), call.owner, true, ChangeKind.CONSTRUCTOR_REPLACED);
                    if (change == null || pendingNew == null || !(pendingNew.getNext() instanceof InsnNode dup)
                            || dup.getOpcode() != Opcodes.DUP) continue; // e.g. super(...) of a plugin subclass
                    instructions.remove(dup);
                    instructions.remove(pendingNew);
                    instructions.set(call, shimCall(change));
                    report.hit(change, node.name);
                    modified = true;
                }
                case MethodInsnNode call -> {
                    boolean isStatic = call.getOpcode() == Opcodes.INVOKESTATIC;
                    for (Change change : step.methods(call.name, call.desc)) {
                        if (!ownerMatches(call.owner, change.owner(), false)) continue;
                        if (change.kind() == ChangeKind.NOTE) {
                            report.hit(change, node.name);
                            continue;
                        }
                        if (change.isStatic() != isStatic) continue;
                        if (change.kind() == ChangeKind.METHOD_RENAMED) {
                            call.name = Objects.requireNonNull(change.newName());
                        } else if (change.kind() == ChangeKind.METHOD_REPLACED) {
                            instructions.set(call, shimCall(change));
                        } else {
                            continue;
                        }
                        report.hit(change, node.name);
                        modified = true;
                        break;
                    }
                }
                case FieldInsnNode field -> {
                    boolean isStatic = field.getOpcode() == Opcodes.GETSTATIC || field.getOpcode() == Opcodes.PUTSTATIC;
                    boolean read = field.getOpcode() == Opcodes.GETSTATIC || field.getOpcode() == Opcodes.GETFIELD;
                    for (Change change : step.fields(field.name, field.desc)) {
                        if (!ownerMatches(field.owner, change.owner(), false)) continue;
                        if (change.kind() == ChangeKind.NOTE) {
                            report.hit(change, node.name);
                            continue;
                        }
                        if (change.isStatic() != isStatic) continue;
                        if (change.kind() == ChangeKind.FIELD_RENAMED) {
                            field.name = Objects.requireNonNull(change.newName());
                        } else if (change.kind() == ChangeKind.FIELD_REPLACED && read) {
                            instructions.set(field, shimCall(change));
                        } else {
                            continue; // writes to replaced fields can't be redirected, they fail when linked
                        }
                        report.hit(change, node.name);
                        modified = true;
                        break;
                    }
                }
                case InvokeDynamicInsnNode indy -> modified |= applyToInvokeDynamic(step, node, indy);
                default -> {
                }
            }
        }

        return modified;
    }

    @Nullable
    private static TypeInsnNode popNew(@NotNull Deque<TypeInsnNode> pendingNews, @NotNull String owner) {
        for (Iterator<TypeInsnNode> it = pendingNews.iterator(); it.hasNext(); ) {
            TypeInsnNode candidate = it.next();
            if (candidate.desc.equals(owner)) {
                it.remove();
                return candidate;
            }
        }
        return null;
    }

    /**
     * Lambdas / method references ({@code guild::getDefaultChannel}) and enum switches.
     */
    private boolean applyToInvokeDynamic(@NotNull Step step, @NotNull ClassNode node, @NotNull InvokeDynamicInsnNode indy) {
        boolean modified = false;
        for (int i = 0; i < indy.bsmArgs.length; i++) {
            if (!(indy.bsmArgs[i] instanceof Handle handle)) continue;
            Handle replacement = applyToHandle(step, node, handle);
            if (replacement != null) {
                indy.bsmArgs[i] = replacement;
                modified = true;
            }
        }

        // switch (permission) { case SEND_POLLS -> ... } compiles to enumSwitch with the constant names as labels
        if (indy.bsm.getOwner().equals(SWITCH_BOOTSTRAPS) && indy.bsm.getName().equals("enumSwitch")) {
            Type[] args = Type.getArgumentTypes(indy.desc);
            if (args.length > 0 && args[0].getSort() == Type.OBJECT) {
                String enumType = args[0].getInternalName();
                for (int i = 0; i < indy.bsmArgs.length; i++) {
                    if (!(indy.bsmArgs[i] instanceof String label)) continue;
                    Change change = find(step.fields(label, args[0].getDescriptor()), enumType, false, ChangeKind.FIELD_RENAMED);
                    if (change == null) continue;
                    indy.bsmArgs[i] = change.newName();
                    report.hit(change, node.name);
                    modified = true;
                }
            }
        }
        return modified;
    }

    @Nullable
    private Handle applyToHandle(@NotNull Step step, @NotNull ClassNode node, @NotNull Handle handle) {
        int tag = handle.getTag();
        if (tag <= Opcodes.H_PUTSTATIC) return null; // field handles aren't available to plugins

        boolean constructor = tag == Opcodes.H_NEWINVOKESPECIAL;
        boolean isStatic = tag == Opcodes.H_INVOKESTATIC;
        for (Change change : step.methods(handle.getName(), handle.getDesc())) {
            if (!ownerMatches(handle.getOwner(), change.owner(), constructor)) continue;
            switch (change.kind()) {
                case NOTE -> report.hit(change, node.name);
                case METHOD_RENAMED -> {
                    if (change.isStatic() != isStatic) continue;
                    report.hit(change, node.name);
                    return new Handle(tag, handle.getOwner(), change.newName(), handle.getDesc(), handle.isInterface());
                }
                case METHOD_REPLACED, CONSTRUCTOR_REPLACED -> {
                    if (change.kind() == ChangeKind.METHOD_REPLACED && (constructor || change.isStatic() != isStatic)) continue;
                    if (change.kind() == ChangeKind.CONSTRUCTOR_REPLACED && !constructor) continue;
                    // The receiver of an instance method becomes the first argument, which is what the shim expects
                    report.hit(change, node.name);
                    return new Handle(Opcodes.H_INVOKESTATIC, change.shimOwner(), change.shimName(),
                            change.shimDescriptor(), false);
                }
                default -> {
                }
            }
        }
        return null;
    }

    @NotNull
    private static MethodInsnNode shimCall(@NotNull Change change) {
        return new MethodInsnNode(Opcodes.INVOKESTATIC, change.shimOwner(), change.shimName(), change.shimDescriptor(), false);
    }

    @Nullable
    private Change find(@NotNull List<Change> candidates, @NotNull String owner, boolean exactOwner, @NotNull ChangeKind kind) {
        for (Change change : candidates)
            if (change.kind() == kind && ownerMatches(owner, change.owner(), exactOwner)) return change;
        return null;
    }

    /**
     * Members are referenced through the static type of the receiver, which may be a subtype of the declaring class.
     */
    private boolean ownerMatches(@NotNull String referenced, @NotNull String changed, boolean exact) {
        if (referenced.equals(changed)) return true;
        return !exact && resolver.isSubtypeOf(referenced, changed);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean inherits(@NotNull ClassNode node, @NotNull String type) {
        List<String> supers = new ArrayList<>(node.interfaces);
        if (node.superName != null) supers.add(node.superName);
        for (String s : supers)
            if (s.equals(type) || resolver.isSubtypeOf(s, type)) return true;
        return false;
    }

    private static boolean references(@NotNull ClassNode node, @NotNull String type) {
        if (type.equals(node.superName) || node.interfaces.contains(type)) return true;
        String descriptorPart = "L" + type + ";";
        for (FieldNode field : node.fields)
            if (field.desc.contains(descriptorPart)) return true;
        for (MethodNode method : node.methods) {
            if (method.desc.contains(descriptorPart)) return true;
            for (AbstractInsnNode insn : method.instructions) {
                boolean hit = switch (insn) {
                    case MethodInsnNode call -> call.owner.equals(type) || call.desc.contains(descriptorPart);
                    case FieldInsnNode field -> field.owner.equals(type) || field.desc.contains(descriptorPart);
                    case TypeInsnNode typeInsn -> typeInsn.desc.equals(type) || typeInsn.desc.contains(descriptorPart);
                    case LdcInsnNode ldc -> ldc.cst instanceof Type t && t.getDescriptor().contains(descriptorPart);
                    case InvokeDynamicInsnNode indy -> indy.desc.contains(descriptorPart);
                    default -> false;
                };
                if (hit) return true;
            }
        }
        return false;
    }

    // --- class renames ---

    private final class RenamingRemapper extends Remapper {
        private final @NotNull Step step;
        private final @NotNull String className;
        boolean changed;

        RenamingRemapper(@NotNull Step step, @NotNull String className) {
            super(Opcodes.ASM9);
            this.step = step;
            this.className = className;
        }

        @Override
        public String map(String internalName) {
            for (Change change : step.classRenames) {
                String renamed = null;
                if (internalName.equals(change.owner())) renamed = change.newName();
                else if (internalName.startsWith(change.owner() + "$"))
                    renamed = change.newName() + internalName.substring(change.owner().length());
                if (renamed != null) {
                    changed = true;
                    report.hit(change, className);
                    return renamed;
                }
            }
            return internalName;
        }
    }

    // --- index ---

    /**
     * One changelog, indexed by member name and descriptor.
     */
    private static final class Step {
        final @NotNull Map<String, List<Change>> methods = new HashMap<>();
        final @NotNull Map<String, List<Change>> fields = new HashMap<>();
        final @NotNull List<Change> classRenames = new ArrayList<>();
        final @NotNull List<Change> classNotes = new ArrayList<>();
        final @NotNull Set<String> constructorOwners = new HashSet<>();
        final @NotNull List<Change> bridges = new ArrayList<>();

        Step(@NotNull ApiChangelog changelog) {
            for (Change change : changelog.changes()) {
                switch (change.kind()) {
                    case CLASS_RENAMED -> classRenames.add(change);
                    case METHOD_RENAMED, METHOD_REPLACED -> add(methods, change);
                    case CONSTRUCTOR_REPLACED -> {
                        add(methods, change);
                        constructorOwners.add(change.owner());
                    }
                    case FIELD_RENAMED, FIELD_REPLACED -> add(fields, change);
                    case ABSTRACT_METHOD_CHANGED -> bridges.add(change);
                    case NOTE -> {
                        String descriptor = change.descriptor();
                        if (change.name() == null) {
                            classNotes.add(change);
                        } else if (descriptor == null) {
                            // Matches fields and methods of that name
                            add(fields, change);
                            add(methods, change);
                        } else {
                            add(descriptor.startsWith("(") ? methods : fields, change);
                        }
                    }
                }
            }
        }

        private static void add(@NotNull Map<String, List<Change>> index, @NotNull Change change) {
            index.computeIfAbsent(Objects.requireNonNull(change.name()), k -> new ArrayList<>()).add(change);
        }

        @NotNull
        List<Change> methods(@NotNull String name, @NotNull String descriptor) {
            return matching(methods, name, descriptor);
        }

        @NotNull
        List<Change> fields(@NotNull String name, @NotNull String descriptor) {
            return matching(fields, name, descriptor);
        }

        @NotNull
        private static List<Change> matching(@NotNull Map<String, List<Change>> index, @NotNull String name,
                                             @NotNull String descriptor) {
            List<Change> candidates = index.get(name);
            if (candidates == null) return List.of();
            List<Change> result = new ArrayList<>(candidates.size());
            for (Change change : candidates)
                if (change.matchesMember(name, descriptor)) result.add(change);
            return result;
        }
    }
}
