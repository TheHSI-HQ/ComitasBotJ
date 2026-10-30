package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Called by the code {@link SandboxTransformer} injects into plugin classes.
 * <p>
 * This is the only core class visible to plugins, but plugins can't reference it in their own bytecode.
 * The caller arguments are class constants injected by the transformer.
 */
@SuppressWarnings({"unused", "deprecation"})
public final class SandboxRuntime {
    private static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Sandbox");
    private static final Set<String> reported = ConcurrentHashMap.newKeySet();
    private static final Set<String> ALLOWED_URL_PROTOCOLS = Set.of("http", "https");

    private SandboxRuntime() {
    }

    @NotNull
    public static RuntimeException violation(@NotNull String message) {
        if (reported.size() < 10_000 && reported.add(message))
            logger.warn("Sandbox violation: {}", message);
        return new SandboxViolationException(message);
    }

    // --- Resources: only from the plugin's own jar ---

    @Nullable
    public static InputStream getResourceAsStream(@NotNull Class<?> target, @NotNull String name, @NotNull Class<?> caller) {
        if (!(caller.getClassLoader() instanceof SandboxPluginClassLoader loader)) return null;

        String resolved;
        if (name.startsWith("/")) {
            resolved = name.substring(1);
        } else {
            String pkg = target.getPackageName();
            resolved = pkg.isEmpty() ? name : pkg.replace('.', '/') + "/" + name;
        }

        URL resource = loader.findResource(resolved);
        if (resource == null) return null;
        try {
            return resource.openStream();
        } catch (IOException e) {
            return null;
        }
    }

    // --- Console output: through the plugin's logger ---

    @NotNull
    public static PrintStream stdout(@NotNull Class<?> caller) {
        return sandboxOf(caller).stdout();
    }

    @NotNull
    public static PrintStream stderr(@NotNull Class<?> caller) {
        return sandboxOf(caller).stderr();
    }

    // --- Files: only in the plugin's own file system ---

    @NotNull
    public static Path pathOf(@NotNull String first, @NotNull String[] more, @NotNull Class<?> caller) {
        return sandboxOf(caller).fileSystem().getPath(first, more);
    }

    @NotNull
    public static FileSystem defaultFileSystem(@NotNull Class<?> caller) {
        return sandboxOf(caller).fileSystem();
    }

    @NotNull
    private static PluginSandbox sandboxOf(@NotNull Class<?> caller) {
        if (!(caller.getClassLoader() instanceof SandboxPluginClassLoader loader))
            throw violation("File system access outside of a plugin");
        return loader.sandbox();
    }

    // --- URLs: only http(s) ---

    @NotNull
    public static URL newUrl(@NotNull String spec) throws MalformedURLException {
        return checkUrl(new URL(spec));
    }

    @NotNull
    public static URL newUrl(@Nullable URL context, @NotNull String spec) throws MalformedURLException {
        return checkUrl(new URL(context, spec));
    }

    @NotNull
    public static URL newUrl(@NotNull String protocol, @NotNull String host, int port, @NotNull String file) throws MalformedURLException {
        return checkUrl(new URL(protocol, host, port, file));
    }

    @NotNull
    public static URL newUrl(@NotNull String protocol, @NotNull String host, @NotNull String file) throws MalformedURLException {
        return checkUrl(new URL(protocol, host, file));
    }

    @NotNull
    public static URL toUrl(@NotNull URI uri) throws MalformedURLException {
        return checkUrl(uri.toURL());
    }

    @NotNull
    private static URL checkUrl(@NotNull URL url) {
        if (!ALLOWED_URL_PROTOCOLS.contains(url.getProtocol().toLowerCase(Locale.ROOT)))
            throw violation("Plugins may only use http and https URLs, not " + url.getProtocol());
        return url;
    }

    // --- Reflection: only on the plugin's own classes ---

    private static void requireOwn(@NotNull Class<?> target, @NotNull Class<?> caller) {
        if (!isOwn(target, caller))
            throw violation("Plugins may only use reflection on their own classes, not on " + target.getName());
    }

    private static boolean isOwn(@NotNull Class<?> target, @NotNull Class<?> caller) {
        ClassLoader loader = caller.getClassLoader();
        return loader instanceof SandboxPluginClassLoader && !target.isArray() && target.getClassLoader() == loader;
    }

    public static Field[] getDeclaredFields(Class<?> target, Class<?> caller) {
        requireOwn(target, caller);
        return target.getDeclaredFields();
    }

    public static Field[] getFields(Class<?> target, Class<?> caller) {
        requireOwn(target, caller);
        return Arrays.stream(target.getFields()).filter(f -> isOwn(f.getDeclaringClass(), caller)).toArray(Field[]::new);
    }

    public static Field getDeclaredField(Class<?> target, String name, Class<?> caller) throws NoSuchFieldException {
        requireOwn(target, caller);
        return target.getDeclaredField(name);
    }

    public static Field getField(Class<?> target, String name, Class<?> caller) throws NoSuchFieldException {
        requireOwn(target, caller);
        Field field = target.getField(name);
        if (!isOwn(field.getDeclaringClass(), caller)) throw new NoSuchFieldException(name);
        return field;
    }

    public static Method[] getDeclaredMethods(Class<?> target, Class<?> caller) {
        requireOwn(target, caller);
        return target.getDeclaredMethods();
    }

    public static Method[] getMethods(Class<?> target, Class<?> caller) {
        requireOwn(target, caller);
        return Arrays.stream(target.getMethods()).filter(m -> isOwn(m.getDeclaringClass(), caller)).toArray(Method[]::new);
    }

    public static Method getDeclaredMethod(Class<?> target, String name, Class<?>[] types, Class<?> caller) throws NoSuchMethodException {
        requireOwn(target, caller);
        return target.getDeclaredMethod(name, types);
    }

    public static Method getMethod(Class<?> target, String name, Class<?>[] types, Class<?> caller) throws NoSuchMethodException {
        requireOwn(target, caller);
        Method method = target.getMethod(name, types);
        if (!isOwn(method.getDeclaringClass(), caller)) throw new NoSuchMethodException(name);
        return method;
    }

    public static Constructor<?>[] getDeclaredConstructors(Class<?> target, Class<?> caller) {
        requireOwn(target, caller);
        return target.getDeclaredConstructors();
    }

    public static Constructor<?>[] getConstructors(Class<?> target, Class<?> caller) {
        requireOwn(target, caller);
        return target.getConstructors();
    }

    public static Constructor<?> getDeclaredConstructor(Class<?> target, Class<?>[] types, Class<?> caller) throws NoSuchMethodException {
        requireOwn(target, caller);
        return target.getDeclaredConstructor(types);
    }

    public static Constructor<?> getConstructor(Class<?> target, Class<?>[] types, Class<?> caller) throws NoSuchMethodException {
        requireOwn(target, caller);
        return target.getConstructor(types);
    }

    public static RecordComponent[] getRecordComponents(Class<?> target, Class<?> caller) {
        requireOwn(target, caller);
        return target.getRecordComponents();
    }

    public static Method getEnclosingMethod(Class<?> target, Class<?> caller) {
        requireOwn(target, caller);
        Method method = target.getEnclosingMethod();
        return method == null || isOwn(method.getDeclaringClass(), caller) ? method : null;
    }

    public static Constructor<?> getEnclosingConstructor(Class<?> target, Class<?> caller) {
        requireOwn(target, caller);
        Constructor<?> constructor = target.getEnclosingConstructor();
        return constructor == null || isOwn(constructor.getDeclaringClass(), caller) ? constructor : null;
    }

    public static Object newInstance(Class<?> target, Class<?> caller) throws InstantiationException, IllegalAccessException {
        requireOwn(target, caller);
        return target.newInstance();
    }
}
