package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Plugin.PluginPermission;
import cloud.thehsi.ComitasBotJ.Configuration.DataPaths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Loads the classes of one plugin jar, after verifying and rewriting them with {@link SandboxTransformer}.
 * <p>
 * Classes from the jar are loaded child first, then the plugin's libraries ({@link PluginLibraryClassLoader},
 * only with {@link PluginPermission#LIBRARIES}), everything else comes from {@link SandboxParentClassLoader}.
 * Classes in reserved packages (java.*, the Comitas core, JDA, ...) are never loaded from the jar.
 */
public final class SandboxPluginClassLoader extends URLClassLoader {
    static {
        registerAsParallelCapable();
    }

    private final @NotNull PluginSandbox sandbox;
    private final @NotNull Set<String> entries;
    private final @NotNull SandboxTransformer transformer;
    private final @Nullable PluginLibraryClassLoader libraries;

    public SandboxPluginClassLoader(@NotNull File jar, @NotNull PluginSandbox sandbox) throws IOException {
        this(jar, sandbox, List.of());
    }

    /**
     * @param libraries Paths of library jars inside the plugin jar, see {@link #parseLibraries(Properties)}.
     *                  They are only loaded, if the plugin was granted {@link PluginPermission#LIBRARIES}.
     */
    public SandboxPluginClassLoader(@NotNull File jar, @NotNull PluginSandbox sandbox,
                                    @NotNull List<String> libraries) throws IOException {
        this(jar, sandbox, libraries, DataPaths.LIBRARIES);
    }

    /**
     * @param libraryCache The directory, library jars are extracted to
     */
    SandboxPluginClassLoader(@NotNull File jar, @NotNull PluginSandbox sandbox, @NotNull List<String> libraries,
                             @NotNull Path libraryCache) throws IOException {
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

        try {
            this.libraries = libraries.isEmpty() || !sandbox.has(PluginPermission.LIBRARIES)
                    ? null : PluginLibraryClassLoader.create(jar, libraries, sandbox.pluginName(), libraryCache);
        } catch (IOException | RuntimeException e) {
            close();
            throw e;
        }

        this.transformer = new SandboxTransformer(sandbox, new ClassHierarchy(this));
    }

    /**
     * Parses the {@code libraries} property of a plugin.properties: paths of jars inside the plugin jar.
     */
    @NotNull
    public static List<String> parseLibraries(@NotNull Properties props) {
        List<String> libraries = new ArrayList<>();
        for (String library : props.getProperty("libraries", "").split("[,\\s]+")) {
            String path = library.strip();
            while (path.startsWith("/")) path = path.substring(1);
            if (!path.isEmpty() && !libraries.contains(path)) libraries.add(path);
        }
        return List.copyOf(libraries);
    }

    @NotNull
    public PluginSandbox sandbox() {
        return sandbox;
    }

    boolean ownsClass(@NotNull String internalName) {
        return !SandboxPolicy.isReserved(internalName) && entries.contains(internalName + ".class");
    }

    /**
     * @return True if the class comes from one of the plugin's (unsandboxed) libraries
     */
    boolean isLibraryClass(@NotNull String internalName) {
        return libraries != null && !ownsClass(internalName) && libraries.isLibraryClass(internalName);
    }

    /**
     * Loads a library class without initializing it.
     *
     * @return The class, or null if it isn't a library class or couldn't be loaded
     */
    @Nullable
    Class<?> libraryClass(@NotNull String internalName) {
        if (libraries == null || !isLibraryClass(internalName)) return null;
        try {
            return Class.forName(internalName.replace('/', '.'), false, libraries);
        } catch (ClassNotFoundException | LinkageError e) {
            return null;
        }
    }

    byte @Nullable [] readOwnClass(@NotNull String internalName) {
        if (!ownsClass(internalName)) return null;
        // Not URL#openStream, it would keep the jar open in the JVM wide jar cache, even after close()
        try (InputStream in = getResourceAsStream(internalName + ".class")) {
            return in == null ? null : in.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected Class<?> loadClass(@NotNull String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = findLoadedClass(name);
            if (clazz == null) {
                String internalName = name.replace('.', '/');
                if (ownsClass(internalName)) clazz = findClass(name);
                else if (libraries != null && isLibraryClass(internalName)) clazz = libraries.loadClass(name);
                else clazz = getParent().loadClass(name);
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

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            // Null while the constructor fails
            //noinspection ConstantValue
            if (libraries != null) libraries.close();
        }
    }
}
