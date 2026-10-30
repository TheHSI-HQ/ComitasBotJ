package cloud.thehsi.ComitasBotJ.Sandbox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the class hierarchy of plugin classes (read from the jar, without loading them)
 * and host classes (through reflection, without initializing them).
 */
final class ClassHierarchy {
    private static final ClassLoader HOST_LOADER = ClassHierarchy.class.getClassLoader();
    private static final Map<String, Optional<Class<?>>> HOST_CACHE = new ConcurrentHashMap<>();

    private final @NotNull SandboxPluginClassLoader loader;
    private final @NotNull Map<String, Optional<OwnClassInfo>> ownCache = new ConcurrentHashMap<>();

    ClassHierarchy(@NotNull SandboxPluginClassLoader loader) {
        this.loader = loader;
    }

    @NotNull
    static String internalName(@NotNull Class<?> clazz) {
        return Type.getInternalName(clazz);
    }

    @Nullable
    static Class<?> hostClass(@NotNull String internalName) {
        return HOST_CACHE.computeIfAbsent(internalName, name -> {
            try {
                return Optional.of(Class.forName(name.replace('/', '.'), false, HOST_LOADER));
            } catch (ClassNotFoundException | LinkageError e) {
                return Optional.empty();
            }
        }).orElse(null);
    }

    /**
     * Finds the class, which declares the member, the way the JVM resolves it
     * (super classes first, then interfaces, then {@link Object} for interfaces).
     */
    @Nullable
    static Class<?> findDeclaring(@NotNull Class<?> clazz, @NotNull String name, @NotNull String descriptor, boolean field) {
        if (name.equals("<init>") || name.equals("<clinit>")) {
            if (field) return null;
            for (var constructor : clazz.getDeclaredConstructors())
                if (Type.getConstructorDescriptor(constructor).equals(descriptor)) return clazz;
            return null;
        }

        for (Class<?> c = clazz; c != null; c = c.getSuperclass())
            if (declares(c, name, descriptor, field)) return c;

        Deque<Class<?>> queue = new ArrayDeque<>();
        Set<Class<?>> seen = new HashSet<>();
        for (Class<?> c = clazz; c != null; c = c.getSuperclass())
            queue.addAll(List.of(c.getInterfaces()));

        while (!queue.isEmpty()) {
            Class<?> c = queue.poll();
            if (!seen.add(c)) continue;
            if (declares(c, name, descriptor, field)) return c;
            queue.addAll(List.of(c.getInterfaces()));
        }

        if (clazz.isInterface() && !field && declares(Object.class, name, descriptor, false))
            return Object.class;
        return null;
    }

    private static boolean declares(@NotNull Class<?> c, @NotNull String name, @NotNull String descriptor, boolean field) {
        try {
            if (field) {
                for (Field f : c.getDeclaredFields())
                    if (f.getName().equals(name) && Type.getDescriptor(f.getType()).equals(descriptor)) return true;
            } else {
                for (Method m : c.getDeclaredMethods())
                    if (m.getName().equals(name) && Type.getMethodDescriptor(m).equals(descriptor)) return true;
            }
        } catch (LinkageError ignored) {
        }
        return false;
    }

    boolean isOwn(@NotNull String internalName) {
        return loader.ownsClass(internalName);
    }

    @Nullable
    OwnClassInfo own(@NotNull String internalName) {
        return ownCache.computeIfAbsent(internalName, name -> {
            byte[] bytes = loader.readOwnClass(name);
            if (bytes == null) return Optional.empty();
            OwnClassInfo info = new OwnClassInfo();
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public void visit(int version, int access, String n, String signature, String superName, String[] interfaces) {
                    info.access = access;
                    info.superName = superName;
                    info.interfaces = interfaces == null ? List.of() : List.of(interfaces);
                }

                @Override
                public FieldVisitor visitField(int access, String n, String descriptor, String signature, Object value) {
                    info.fields.add(n + ":" + descriptor);
                    return null;
                }

                @Override
                public MethodVisitor visitMethod(int access, String n, String descriptor, String signature, String[] exceptions) {
                    info.methods.add(n + descriptor);
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return Optional.of(info);
        }).orElse(null);
    }

    @Nullable
    String superName(@NotNull String internalName) {
        if (isOwn(internalName)) {
            OwnClassInfo info = own(internalName);
            return info == null ? null : info.superName;
        }
        Class<?> host = hostClass(internalName);
        if (host == null || host.getSuperclass() == null) return null;
        return internalName(host.getSuperclass());
    }

    boolean isInterface(@NotNull String internalName) {
        if (isOwn(internalName)) {
            OwnClassInfo info = own(internalName);
            return info != null && (info.access & Opcodes.ACC_INTERFACE) != 0;
        }
        Class<?> host = hostClass(internalName);
        return host != null && host.isInterface();
    }

    /**
     * Used by ASM to compute stack map frames.
     */
    @NotNull
    String commonSuperClass(@NotNull String type1, @NotNull String type2) {
        if (type1.equals(type2)) return type1;
        if (type1.startsWith("[") || type2.startsWith("[")) return "java/lang/Object";
        if (isInterface(type1) || isInterface(type2)) return "java/lang/Object";

        Set<String> chain = new HashSet<>();
        for (String t = type1; t != null; t = superName(t))
            if (!chain.add(t)) break;

        Set<String> seen = new HashSet<>();
        for (String t = type2; t != null && seen.add(t); t = superName(t))
            if (chain.contains(t)) return t;

        return "java/lang/Object";
    }

    static final class OwnClassInfo {
        final @NotNull Set<String> methods = new HashSet<>();
        final @NotNull Set<String> fields = new HashSet<>();
        int access;
        @Nullable String superName;
        @NotNull List<String> interfaces = List.of();
    }
}
