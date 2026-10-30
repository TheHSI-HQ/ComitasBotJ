package cloud.thehsi.ComitasBotJ.Sandbox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Loads the classes of one plugin jar, after verifying and rewriting them with {@link SandboxTransformer}.
 * <p>
 * Classes from the jar are loaded child first, everything else comes from {@link SandboxParentClassLoader}.
 * Classes in reserved packages (java.*, the Comitas core, JDA, ...) are never loaded from the jar.
 */
public final class SandboxPluginClassLoader extends URLClassLoader {
    static {
        registerAsParallelCapable();
    }

    private final @NotNull PluginSandbox sandbox;
    private final @NotNull Set<String> entries;
    private final @NotNull SandboxTransformer transformer;

    public SandboxPluginClassLoader(@NotNull File jar, @NotNull PluginSandbox sandbox) throws IOException {
        super("plugin-" + sandbox.pluginName(), new URL[]{jar.toURI().toURL()}, SandboxParentClassLoader.INSTANCE);
        this.sandbox = sandbox;

        try (JarFile jarFile = new JarFile(jar)) {
            entries = jarFile.stream().map(JarEntry::getName).collect(Collectors.toUnmodifiableSet());
        }

        for (String entry : entries) {
            if (entry.startsWith("net/dv8tion/")) {
                close();
                throw new SandboxViolationException("Plugin " + sandbox.pluginName()
                        + " bundles JDA (" + entry + "), plugins must not access Discord directly, use the Comitas API");
            }
        }

        this.transformer = new SandboxTransformer(sandbox, new ClassHierarchy(this));
    }

    @NotNull
    public PluginSandbox sandbox() {
        return sandbox;
    }

    boolean ownsClass(@NotNull String internalName) {
        return !SandboxPolicy.isReserved(internalName) && entries.contains(internalName + ".class");
    }

    byte @Nullable [] readOwnClass(@NotNull String internalName) {
        if (!ownsClass(internalName)) return null;
        URL resource = findResource(internalName + ".class");
        if (resource == null) return null;
        try (InputStream in = resource.openStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected Class<?> loadClass(@NotNull String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = findLoadedClass(name);
            if (clazz == null) {
                clazz = ownsClass(name.replace('.', '/'))
                        ? findClass(name)
                        : getParent().loadClass(name);
            }
            if (resolve) resolveClass(clazz);
            return clazz;
        }
    }

    @Override
    protected Class<?> findClass(@NotNull String name) throws ClassNotFoundException {
        String internalName = name.replace('.', '/');
        byte[] bytes = readOwnClass(internalName);
        if (bytes == null) throw new ClassNotFoundException(name);

        byte[] transformed;
        try {
            transformed = transformer.transform(internalName, bytes);
        } catch (SandboxViolationException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new SandboxViolationException("Plugin " + sandbox.pluginName()
                    + ": class " + name + " could not be verified", e);
        }

        return defineClass(name, transformed, 0, transformed.length);
    }

    /**
     * Resources are only looked up in the plugin's own jar.
     */
    @Override
    @Nullable
    public URL getResource(@NotNull String name) {
        return findResource(name);
    }

    @Override
    public Enumeration<URL> getResources(@NotNull String name) throws IOException {
        return findResources(name);
    }
}
