package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Plugin.PluginPermission;
import cloud.thehsi.ComitasBotJ.Compatibility.CompatibilityPlan;
import cloud.thehsi.ComitasBotJ.Compatibility.CompatibilityReport;
import cloud.thehsi.ComitasBotJ.Compatibility.CompatibilityTransformer;
import cloud.thehsi.ComitasBotJ.Configuration.DataPaths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipFile;

/**
 * Loads the classes of one plugin jar, after verifying and rewriting them with {@link SandboxTransformer}.
 * <p>
 * Plugins built against an older API first go through the {@link CompatibilityTransformer}, so the sandbox
 * verifies the code, which actually runs.
 * <p>
 * Classes from the jar (and its sandboxed libraries, only with {@link PluginPermission#LIBRARIES}) are
 * loaded child first, then the plugin's risky libraries ({@link RiskyLibraryClassLoader}, only with
 * {@link PluginPermission#RISKY_LIBRARIES}), everything else comes from {@link SandboxParentClassLoader}.
 * Classes in reserved packages (java.*, the Comitas core, JDA, ...) are never loaded from the jar.
 * <p>
 * The {@link SandboxJavaApplication}s of a plugin ({@link PluginPermission#JAVA_APPLICATIONS}) are loaded by
 * their own instance of this class loader, with the plugin's sandbox, see {@link #loadApplication(Path, String)}.
 */
public final class SandboxPluginClassLoader extends URLClassLoader {
    static {
        registerAsParallelCapable();
    }

    private final @NotNull PluginSandbox sandbox;
    /**
     * The plugin's class loader, if this one loads an application of the plugin, otherwise null.
     */
    private final @Nullable SandboxPluginClassLoader owner;
    private final @NotNull Set<String> entries;
    private final @NotNull SandboxTransformer transformer;
    private final @Nullable RiskyLibraryClassLoader riskyLibraries;
    private final @Nullable CompatibilityPlan compatibilityPlan;
    private final @Nullable CompatibilityTransformer compatibility;
    private final @NotNull Set<SandboxJavaApplication> applications = ConcurrentHashMap.newKeySet();
    private volatile boolean closed;

    public SandboxPluginClassLoader(@NotNull File jar, @NotNull PluginSandbox sandbox) throws IOException {
        this(jar, sandbox, List.of());
    }

    /**
     * @param riskyLibraries Paths of library jars inside the plugin jar, see {@link #parseRiskyLibraries(Properties)}.
     *                       They are only loaded, if the plugin was granted {@link PluginPermission#RISKY_LIBRARIES}.
     */
    public SandboxPluginClassLoader(@NotNull File jar, @NotNull PluginSandbox sandbox,
                                    @NotNull List<String> riskyLibraries) throws IOException {
        this(jar, sandbox, riskyLibraries, (CompatibilityPlan) null);
    }

    /**
     * @param compatibilityPlan The changes to apply, if the plugin was built against an older API, otherwise null
     */
    public SandboxPluginClassLoader(@NotNull File jar, @NotNull PluginSandbox sandbox, @NotNull List<String> riskyLibraries,
                                    @Nullable CompatibilityPlan compatibilityPlan) throws IOException {
        this(jar, sandbox, riskyLibraries, List.of(), compatibilityPlan);
    }

    /**
     * @param libraries Paths of library jars inside the plugin jar, see {@link #parseLibraries(Properties)}.
     *                  They are only loaded, if the plugin was granted {@link PluginPermission#LIBRARIES}.
     */
    public SandboxPluginClassLoader(@NotNull File jar, @NotNull PluginSandbox sandbox, @NotNull List<String> riskyLibraries,
                                    @NotNull List<String> libraries,
                                    @Nullable CompatibilityPlan compatibilityPlan) throws IOException {
        this(jar, sandbox, riskyLibraries, libraries, DataPaths.LIBRARIES, compatibilityPlan);
    }

    /**
     * @param libraryCache The directory, library jars are extracted to
     */
    SandboxPluginClassLoader(@NotNull File jar, @NotNull PluginSandbox sandbox, @NotNull List<String> riskyLibraries,
                             @NotNull Path libraryCache) throws IOException {
        this(jar, sandbox, riskyLibraries, libraryCache, (CompatibilityPlan) null);
    }

    SandboxPluginClassLoader(@NotNull File jar, @NotNull PluginSandbox sandbox, @NotNull List<String> riskyLibraries,
                             @NotNull Path libraryCache, @Nullable CompatibilityPlan compatibilityPlan) throws IOException {
        this(jar, sandbox, riskyLibraries, List.of(), libraryCache, compatibilityPlan);
    }

    SandboxPluginClassLoader(@NotNull File jar, @NotNull PluginSandbox sandbox, @NotNull List<String> riskyLibraries,
                             @NotNull List<String> libraries, @NotNull Path libraryCache,
                             @Nullable CompatibilityPlan compatibilityPlan) throws IOException {
        this("plugin-" + sandbox.pluginName(), pluginJars(jar, sandbox, libraries, libraryCache), sandbox,
                null, jar, riskyLibraries, libraryCache, compatibilityPlan);
    }

    /**
     * @param jars       The jars, whose classes are sandboxed and belong to this class loader, in lookup order
     * @param owner      The plugin's class loader, if this one loads an application, otherwise null
     * @param libraryJar The jar the risky (unsandboxed) libraries are nested in, null if there are none
     */
    private SandboxPluginClassLoader(@NotNull String name, @NotNull List<Path> jars, @NotNull PluginSandbox sandbox,
                                     @Nullable SandboxPluginClassLoader owner, @Nullable File libraryJar,
                                     @NotNull List<String> riskyLibraries, @NotNull Path libraryCache,
                                     @Nullable CompatibilityPlan compatibilityPlan) throws IOException {
        super(name, urls(jars), SandboxParentClassLoader.INSTANCE);
        this.sandbox = sandbox;
        this.owner = owner;
        this.compatibilityPlan = compatibilityPlan;

        try {
            this.entries = readEntries(jars, sandbox.pluginName());
            this.riskyLibraries = libraryJar == null || riskyLibraries.isEmpty() || !sandbox.has(PluginPermission.RISKY_LIBRARIES)
                    ? null : RiskyLibraryClassLoader.create(libraryJar, riskyLibraries, sandbox.pluginName(), libraryCache);
        } catch (IOException | RuntimeException e) {
            close();
            throw e;
        }

        ClassHierarchy hierarchy = new ClassHierarchy(this);
        this.compatibility = compatibilityPlan == null ? null
                : new CompatibilityTransformer(compatibilityPlan, hierarchy::isSubtypeOf, new CompatibilityReport());
        this.transformer = new SandboxTransformer(sandbox, hierarchy, compatibility, owner != null);
    }

    /**
     * The plugin jar, followed by its (sandboxed) libraries (extracted to the cache), if the plugin may load them.
     */
    @NotNull
    private static List<Path> pluginJars(@NotNull File jar, @NotNull PluginSandbox sandbox,
                                         @NotNull List<String> libraries, @NotNull Path libraryCache) throws IOException {
        List<Path> jars = new ArrayList<>(List.of(jar.toPath()));
        if (libraries.isEmpty() || !sandbox.has(PluginPermission.LIBRARIES)) return jars;
        try (JarFile jarFile = new JarFile(jar)) {
            for (String library : libraries)
                jars.add(RiskyLibraryClassLoader.extract(jarFile, library, sandbox.pluginName(), libraryCache));
        }
        return jars;
    }

    private static URL @NotNull [] urls(@NotNull List<Path> jars) throws MalformedURLException {
        URL[] urls = new URL[jars.size()];
        for (int i = 0; i < urls.length; i++) urls[i] = jars.get(i).toUri().toURL();
        return urls;
    }

    /**
     * @return The entries of all jars, versioned entries of multi-release jars by their base name
     * @throws SandboxViolationException If a jar bundles JDA
     */
    @NotNull
    private static Set<String> readEntries(@NotNull List<Path> jars, @NotNull String pluginName) throws IOException {
        Set<String> entries = new HashSet<>();
        for (Path jar : jars) {
            try (JarFile jarFile = new JarFile(jar.toFile(), true, ZipFile.OPEN_READ, Runtime.version())) {
                for (JarEntry entry : jarFile.versionedStream().toList()) {
                    String name = entry.getName();
                    if (name.startsWith("net/dv8tion/"))
                        throw new SandboxViolationException("Plugin " + pluginName + " bundles JDA (" + name
                                + (jars.size() > 1 ? " in " + jar.getFileName() : "")
                                + "), plugins must not access Discord directly, use the Comitas API");
                    entries.add(name);
                }
            }
        }
        return Collections.unmodifiableSet(entries);
    }

    /**
     * Parses the {@code libraries} property of a plugin.properties: paths of jars inside the plugin jar,
     * which are loaded sandboxed.
     */
    @NotNull
    public static List<String> parseLibraries(@NotNull Properties props) {
        return parseJarList(props, "libraries");
    }

    /**
     * Parses the {@code risky-libraries} property of a plugin.properties: paths of jars inside the plugin jar,
     * which are loaded unsandboxed.
     */
    @NotNull
    public static List<String> parseRiskyLibraries(@NotNull Properties props) {
        return parseJarList(props, "risky-libraries");
    }

    @NotNull
    private static List<String> parseJarList(@NotNull Properties props, @NotNull String key) {
        List<String> libraries = new ArrayList<>();
        for (String library : props.getProperty(key, "").split("[,\\s]+")) {
            String path = library.strip();
            while (path.startsWith("/")) path = path.substring(1);
            if (!path.isEmpty() && !libraries.contains(path)) libraries.add(path);
        }
        return List.copyOf(libraries);
    }

    /**
     * Loads a jar of the plugin's file system as an application, which runs in the plugin's sandbox,
     * with its own class loader. Jars in the {@code Class-Path} of its manifest are loaded as well, if they exist.
     *
     * @param jar       A path of the plugin's file system
     * @param mainClass The class to run, null for the manifest's {@code Main-Class}
     * @throws SecurityException If the plugin wasn't granted {@link PluginPermission#JAVA_APPLICATIONS}
     * @throws IOException       If the jar doesn't exist or can't be read
     */
    @NotNull
    public SandboxJavaApplication loadApplication(@NotNull Path jar, @Nullable String mainClass) throws IOException {
        // An application loading another one, it belongs to the plugin as well
        if (owner != null) return owner.loadApplication(jar, mainClass);
        if (!sandbox.has(PluginPermission.JAVA_APPLICATIONS))
            throw new SecurityException("Plugin " + sandbox.pluginName() + " may not run Java applications, it requires the "
                    + PluginPermission.JAVA_APPLICATIONS.getKey() + " permission");
        if (closed) throw new IllegalStateException("Plugin " + sandbox.pluginName() + " was unloaded");

        Path absolute = jar.toAbsolutePath();
        @SuppressWarnings("resource") Path hostJar = sandbox.fileSystem().hostPath(absolute);
        if (!Files.isRegularFile(hostJar)) throw new NoSuchFileException(absolute.toString());

        List<Path> jars = new ArrayList<>(List.of(hostJar));
        String manifestMain = null;
        try (JarFile jarFile = new JarFile(hostJar.toFile())) {
            Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                manifestMain = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
                String classPath = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
                if (classPath != null) jars.addAll(classPath(absolute, classPath, jars));
            }
        }

        String main = mainClass != null ? mainClass.strip() : manifestMain == null ? null : manifestMain.strip();
        SandboxPluginClassLoader loader = new SandboxPluginClassLoader(
                "application-" + sandbox.pluginName() + "-" + hostJar.getFileName(), List.copyOf(jars), sandbox,
                this, null, List.of(), DataPaths.LIBRARIES, null);
        SandboxJavaApplication application = new SandboxJavaApplication(absolute,
                main == null || main.isEmpty() ? null : main, loader, this);
        applications.add(application);
        // The plugin may have been unloaded meanwhile
        if (closed) application.close();
        return application;
    }

    /**
     * @return The applied changes, if the plugin runs in compatibility mode
     */
    @Nullable
    public CompatibilityPlan compatibilityPlan() {
        return compatibilityPlan;
    }

    /**
     * Runs the compatibility layer over every class of the jar (without loading them), so all affected
     * API usages are known up front, not only those of the classes loaded so far.
     *
     * @return The report, null if the plugin doesn't run in compatibility mode
     */
    @Nullable
    public CompatibilityReport scanCompatibility() {
        if (compatibility == null) return null;
        for (String entry : entries) {
            if (!entry.endsWith(".class") || entry.startsWith("META-INF/")) continue;
            byte[] bytes = readOwnClass(entry.substring(0, entry.length() - ".class".length()));
            if (bytes == null) continue;
            try {
                ClassNode node = new ClassNode();
                new ClassReader(bytes).accept(node, 0);
                compatibility.transform(node);
            } catch (RuntimeException ignored) {
                // Broken classes are reported, once (if ever) they are loaded
            }
        }
        return compatibility.report();
    }

    /**
     * Maps an API class name, as used by the plugin, to the current API (identity outside compatibility mode).
     */
    @NotNull
    String mapClassName(@NotNull String internalName) {
        return compatibilityPlan == null ? internalName : compatibilityPlan.mapClassName(internalName);
    }

    /**
     * Resolves the relative jar entries of a manifest {@code Class-Path} in the plugin's file system.
     * Absolute URLs, directories and missing jars are skipped.
     */
    @NotNull
    private List<Path> classPath(@NotNull Path jar, @NotNull String classPath, @NotNull List<Path> known) {
        List<Path> jars = new ArrayList<>();
        Path directory = jar.getParent();
        if (directory == null) return jars;
        for (String entry : classPath.strip().split("\\s+")) {
            if (entry.isEmpty() || entry.contains(":") || entry.endsWith("/")) continue;
            try {
                String decoded = URLDecoder.decode(entry.replace("+", "%2B"), StandardCharsets.UTF_8);
                @SuppressWarnings("resource") Path host = sandbox.fileSystem().hostPath(directory.resolve(decoded).normalize());
                if (Files.isRegularFile(host) && !known.contains(host) && !jars.contains(host)) jars.add(host);
            } catch (IOException | IllegalArgumentException e) {
                // Not part of the plugin's file system, the application fails, if it really needs it
            }
        }
        return jars;
    }

    void removeApplication(@NotNull SandboxJavaApplication application) {
        applications.remove(application);
    }

    /**
     * @return The class loader of the plugin itself: this one, unless it loads one of the plugin's applications
     */
    @NotNull
    public SandboxPluginClassLoader pluginLoader() {
        return owner == null ? this : owner;
    }

    @NotNull
    public PluginSandbox sandbox() {
        return sandbox;
    }

    boolean ownsClass(@NotNull String internalName) {
        return !SandboxPolicy.isReserved(internalName) && entries.contains(internalName + ".class");
    }

    /**
     * @return True if the class comes from one of the plugin's risky (unsandboxed) libraries
     */
    boolean isLibraryClass(@NotNull String internalName) {
        return riskyLibraries != null && !ownsClass(internalName) && riskyLibraries.isLibraryClass(internalName);
    }

    /**
     * Loads a library class without initializing it.
     *
     * @return The class, or null if it isn't a library class or couldn't be loaded
     */
    @Nullable
    Class<?> libraryClass(@NotNull String internalName) {
        if (riskyLibraries == null || !isLibraryClass(internalName)) return null;
        try {
            return Class.forName(internalName.replace('/', '.'), false, riskyLibraries);
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
                else if (riskyLibraries != null && isLibraryClass(internalName)) clazz = riskyLibraries.loadClass(name);
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
        closed = true;
        for (SandboxJavaApplication application : List.copyOf(applications)) {
            try {
                application.close();
            } catch (IOException ignored) {
                // The plugin's own class loader is closed anyway
            }
        }
        try {
            super.close();
        } finally {
            // Null while the constructor fails
            //noinspection ConstantValue
            if (riskyLibraries != null) riskyLibraries.close();
        }
    }
}
