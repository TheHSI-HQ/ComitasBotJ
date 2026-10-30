package cloud.thehsi.ComitasBotJ.Sandbox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Parent of every plugin class loader.
 * <p>
 * Only exposes JDK classes and the Comitas API (+ slf4j and annotations).
 * Core classes, JDA, Gson, Logback & co. can't be loaded by plugins at all.
 * It doesn't expose any resources.
 */
final class SandboxParentClassLoader extends ClassLoader {
    static final @NotNull SandboxParentClassLoader INSTANCE = new SandboxParentClassLoader();

    static {
        registerAsParallelCapable();
    }

    private final @NotNull ClassLoader core = SandboxParentClassLoader.class.getClassLoader();

    private SandboxParentClassLoader() {
        super("comitas-plugin-sandbox", null);
    }

    @Override
    protected Class<?> loadClass(@NotNull String name, boolean resolve) throws ClassNotFoundException {
        if (name.startsWith("java.") || name.startsWith("javax."))
            return ClassLoader.getPlatformClassLoader().loadClass(name);
        if (SandboxPolicy.isExposedHostClass(name))
            return core.loadClass(name);
        throw new ClassNotFoundException(name);
    }

    @Override
    protected Class<?> findClass(@NotNull String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }

    @Override
    @Nullable
    public URL getResource(@NotNull String name) {
        return null;
    }

    @Override
    @NotNull
    public Enumeration<URL> getResources(@NotNull String name) {
        return Collections.emptyEnumeration();
    }
}
