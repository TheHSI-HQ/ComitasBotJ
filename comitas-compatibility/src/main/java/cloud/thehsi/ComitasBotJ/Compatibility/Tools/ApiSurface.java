package cloud.thehsi.ComitasBotJ.Compatibility.Tools;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * The public API (classes, methods, fields usable by plugins) of a compiled comitas-api jar or class directory.
 */
public final class ApiSurface {
    private final @NotNull Map<String, ClassInfo> classes;

    private ApiSurface(@NotNull Map<String, ClassInfo> classes) {
        this.classes = classes;
    }

    /**
     * @param location A jar file or a directory of class files
     * @param prefix   Only classes in this package (internal name prefix) are read
     */
    @NotNull
    public static ApiSurface read(@NotNull Path location, @NotNull String prefix) throws IOException {
        Map<String, ClassInfo> classes = new TreeMap<>();
        if (Files.isDirectory(location)) {
            try (Stream<Path> files = Files.walk(location)) {
                for (Path file : files.filter(f -> f.toString().endsWith(".class")).toList()) {
                    String name = location.relativize(file).toString().replace('\\', '/');
                    if (!name.startsWith(prefix)) continue;
                    try (InputStream in = Files.newInputStream(file)) {
                        readClass(in, classes);
                    }
                }
            }
        } else {
            try (JarFile jar = new JarFile(location.toFile())) {
                for (JarEntry entry : Collections.list(jar.entries())) {
                    if (!entry.getName().endsWith(".class") || !entry.getName().startsWith(prefix)) continue;
                    try (InputStream in = jar.getInputStream(entry)) {
                        readClass(in, classes);
                    }
                }
            }
        }
        return new ApiSurface(Collections.unmodifiableMap(classes));
    }

    private static void readClass(@NotNull InputStream in, @NotNull Map<String, ClassInfo> classes) throws IOException {
        ClassReader reader = new ClassReader(in);
        ClassInfo info = new ClassInfo();
        reader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                info.name = name;
                info.access = access;
                info.superName = superName;
                info.interfaces = interfaces == null ? List.of() : List.of(interfaces);
            }

            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access) {
                // The real access of a nested class is only stored here (private nested classes are package private in the class file)
                if (name.equals(info.name)) info.access = access | (info.access & Opcodes.ACC_INTERFACE);
            }

            @Override
            public @Nullable FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                if (visible(access)) info.fields.add(name + ":" + descriptor);
                return null;
            }

            @Override
            public @Nullable MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (visible(access) && (access & Opcodes.ACC_BRIDGE) == 0 && !name.equals("<clinit>"))
                    info.methods.add(name + descriptor);
                return null;
            }

            private boolean visible(int access) {
                if ((access & Opcodes.ACC_SYNTHETIC) != 0) return false;
                if ((access & Opcodes.ACC_PUBLIC) != 0) return true;
                // Protected members can only be used by plugin subclasses
                return (access & Opcodes.ACC_PROTECTED) != 0 && (info.access & Opcodes.ACC_FINAL) == 0;
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        if ((info.access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0 && (info.access & Opcodes.ACC_SYNTHETIC) == 0)
            classes.put(info.name, info);
    }

    @NotNull
    public Map<String, ClassInfo> classes() {
        return classes;
    }

    @Nullable
    public ClassInfo get(@NotNull String internalName) {
        return classes.get(internalName);
    }

    /**
     * Whether the member is usable on {@code owner}, either declared there or inherited.
     *
     * @param member {@code name + descriptor} for methods, {@code name:descriptor} for fields
     */
    public boolean hasMember(@NotNull String owner, @NotNull String member, boolean field) {
        ClassInfo info = classes.get(owner);
        if (info == null) return false;
        if ((field ? info.fields : info.methods).contains(member)) return true;
        if (member.startsWith("<init>")) return false; // constructors aren't inherited

        Deque<String> queue = new ArrayDeque<>();
        if (info.superName != null) queue.add(info.superName);
        queue.addAll(info.interfaces);
        Set<String> seen = new HashSet<>();
        while (!queue.isEmpty()) {
            String type = queue.poll();
            if (!seen.add(type)) continue;
            ClassInfo superInfo = classes.get(type);
            if (superInfo != null) {
                if ((field ? superInfo.fields : superInfo.methods).contains(member)) return true;
                if (superInfo.superName != null) queue.add(superInfo.superName);
                queue.addAll(superInfo.interfaces);
            } else if (jdkDeclares(type, member, field)) {
                return true;
            }
        }
        return false;
    }

    private static boolean jdkDeclares(@NotNull String type, @NotNull String member, boolean field) {
        if (!type.startsWith("java/")) return false;
        try {
            Class<?> clazz = Class.forName(type.replace('/', '.'), false, ClassLoader.getPlatformClassLoader());
            if (field) {
                String name = member.substring(0, member.indexOf(':'));
                String descriptor = member.substring(member.indexOf(':') + 1);
                for (var f : clazz.getFields())
                    if (f.getName().equals(name) && org.objectweb.asm.Type.getDescriptor(f.getType()).equals(descriptor)) return true;
            } else {
                for (var m : clazz.getMethods())
                    if ((m.getName() + org.objectweb.asm.Type.getMethodDescriptor(m)).equals(member)) return true;
                for (var m : clazz.getDeclaredMethods())
                    if (java.lang.reflect.Modifier.isProtected(m.getModifiers())
                            && (m.getName() + org.objectweb.asm.Type.getMethodDescriptor(m)).equals(member)) return true;
            }
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
        return false;
    }

    public static final class ClassInfo {
        @NotNull String name = "";
        int access;
        @Nullable String superName;
        @NotNull List<String> interfaces = List.of();
        final @NotNull Set<String> methods = new TreeSet<>();
        final @NotNull Set<String> fields = new TreeSet<>();

        @NotNull
        public String name() {
            return name;
        }

        @NotNull
        public Set<String> methods() {
            return Collections.unmodifiableSet(methods);
        }

        @NotNull
        public Set<String> fields() {
            return Collections.unmodifiableSet(fields);
        }
    }
}
