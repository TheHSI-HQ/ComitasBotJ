package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Plugin.PluginPermission;
import cloud.thehsi.ComitasBotJ.Configuration.DataPaths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

/**
 * Loads the libraries a plugin bundles, if it was granted {@link PluginPermission#LIBRARIES}.
 * <p>
 * Library jars are nested in the plugin jar and listed in its {@code plugin.properties} ({@code libraries=...}).
 * They are extracted to {@link DataPaths#LIBRARIES} and loaded <b>without</b> being verified or rewritten by the sandbox,
 * so they can use native code, files, {@code ServiceLoader} & co. They are trusted code.
 * <p>
 * Like plugins, they only see the JDK and the Comitas API ({@link SandboxParentClassLoader}), so they
 * don't clash with the core's own dependencies and can't name core classes or other plugins' classes.
 */
final class PluginLibraryClassLoader extends URLClassLoader {
    static {
        registerAsParallelCapable();
    }

    /**
     * Internal names of the classes, which are loaded from the library jars.
     */
    private final @NotNull @Unmodifiable Set<String> classes;

    private PluginLibraryClassLoader(@NotNull String pluginName, @NotNull URL[] jars, @NotNull Set<String> classes) {
        super("libraries-" + pluginName, jars, SandboxParentClassLoader.INSTANCE);
        this.classes = Set.copyOf(classes);
    }

    /**
     * Extracts the libraries from the plugin jar and creates a class loader for them.
     *
     * @param libraries      Paths of the library jars inside the plugin jar
     * @param cacheDirectory The directory, the jars are extracted to, usually {@link DataPaths#LIBRARIES}
     * @throws SandboxViolationException If a library bundles JDA
     */
    @NotNull
    static PluginLibraryClassLoader create(@NotNull File pluginJar, @NotNull List<String> libraries,
                                           @NotNull String pluginName, @NotNull Path cacheDirectory) throws IOException {
        List<URL> urls = new ArrayList<>();
        Set<String> classes = new HashSet<>();

        try (JarFile jarFile = new JarFile(pluginJar)) {
            for (String library : libraries) {
                JarEntry entry = jarFile.getJarEntry(library);
                if (entry == null || entry.isDirectory())
                    throw new IOException("Plugin " + pluginName + " lists the library " + library
                            + ", which doesn't exist in its jar");

                byte[] bytes;
                try (InputStream in = jarFile.getInputStream(entry)) {
                    bytes = in.readAllBytes();
                }

                Path extracted = extract(bytes, cacheDirectory);
                classes.addAll(scan(extracted, library, pluginName));
                urls.add(toUrl(extracted));
            }
        }

        return new PluginLibraryClassLoader(pluginName, urls.toArray(URL[]::new), classes);
    }

    /**
     * Writes the jar to the cache, named by its hash, unless it is already there.
     */
    @NotNull
    private static Path extract(byte @NotNull [] bytes, @NotNull Path cacheDirectory) throws IOException {
        Path target = cacheDirectory.resolve(sha256(bytes) + ".jar");
        if (Files.isRegularFile(target) && Files.size(target) == bytes.length) return target;

        Files.createDirectories(cacheDirectory);
        Path temp = Files.createTempFile(cacheDirectory, "library-", ".tmp");
        try {
            Files.write(temp, bytes);
            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            // Windows: the jar may be opened by a library loader of the previous reload, it has the same content
            if (!Files.isRegularFile(target) || Files.size(target) != bytes.length) throw e;
        } finally {
            Files.deleteIfExists(temp);
        }
        return target;
    }

    /**
     * @return Internal names of the classes, this library defines (and the JDK / Comitas API doesn't)
     */
    @NotNull
    private static Set<String> scan(@NotNull Path jar, @NotNull String library, @NotNull String pluginName) throws IOException {
        Set<String> classes = new HashSet<>();
        try (JarFile jarFile = new JarFile(jar.toFile(), true, ZipFile.OPEN_READ, Runtime.version())) {
            for (JarEntry entry : jarFile.versionedStream().toList()) {
                String name = entry.getName();
                if (!name.endsWith(".class") || name.endsWith("module-info.class")) continue;

                String internalName = name.substring(0, name.length() - ".class".length());
                if (internalName.startsWith("net/dv8tion/"))
                    throw new SandboxViolationException("Plugin " + pluginName + ": the library " + library
                            + " bundles JDA (" + name + "), plugins must not access Discord directly, use the Comitas API");
                if (!servedByParent(internalName)) classes.add(internalName);
            }
        }
        return classes;
    }

    /**
     * Classes in reserved packages are never loaded from a library. That are the JDK, the Comitas API, slf4j
     * and the annotations (served by the parent) and core classes (not available at all).
     * {@code javax} classes not part of the JDK (e.g. {@code javax.inject}) may come from a library.
     */
    private static boolean servedByParent(@NotNull String internalName) {
        if (internalName.startsWith("javax/"))
            return ClassLoader.getPlatformClassLoader().getResource(internalName + ".class") != null;
        return SandboxPolicy.isReserved(internalName);
    }

    /**
     * @return True if this class is loaded from one of the libraries
     */
    boolean isLibraryClass(@NotNull String internalName) {
        return classes.contains(internalName);
    }

    @Override
    protected Class<?> loadClass(@NotNull String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> clazz = findLoadedClass(name);
            if (clazz == null) {
                clazz = isLibraryClass(name.replace('.', '/'))
                        ? findClass(name)
                        : getParent().loadClass(name);
            }
            if (resolve) resolveClass(clazz);
            return clazz;
        }
    }

    @NotNull
    private static String sha256(byte @NotNull [] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    @NotNull
    private static URL toUrl(@NotNull Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }
}
