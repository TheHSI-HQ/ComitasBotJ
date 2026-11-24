package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.API.Plugin.JavaApplication;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginPermission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A jar of a plugin's file system, run with {@link PluginPermission#JAVA_APPLICATIONS}.
 * <p>
 * Its classes are loaded by their own {@link SandboxPluginClassLoader} with the plugin's {@link PluginSandbox},
 * so they are verified like the plugin's classes, use its file system and logger, and API calls are attributed
 * to the plugin ({@link SandboxPluginClassLoader#pluginLoader()}).
 */
public final class SandboxJavaApplication implements JavaApplication {
    private static final @NotNull AtomicInteger THREADS = new AtomicInteger();

    private final @NotNull Path jar;
    private final @Nullable String mainClass;
    private final @NotNull SandboxPluginClassLoader loader;
    private final @NotNull SandboxPluginClassLoader owner;
    private volatile boolean closed;

    SandboxJavaApplication(@NotNull Path jar, @Nullable String mainClass, @NotNull SandboxPluginClassLoader loader,
                           @NotNull SandboxPluginClassLoader owner) {
        this.jar = jar;
        this.mainClass = mainClass;
        this.loader = loader;
        this.owner = owner;
    }

    @Override
    public @NotNull Path getJar() {
        return jar;
    }

    @Override
    public @Nullable String getMainClass() {
        return mainClass;
    }

    @Override
    public int run(@NotNull String... args) throws Exception {
        Method main = findMain();
        String[] arguments = args.clone();
        try {
            if (main.getParameterCount() == 1) main.invoke(null, (Object) arguments);
            else main.invoke(null);
            return 0;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ApplicationExitError exit) return exit.status();
            if (cause instanceof Exception exception) throw exception;
            if (cause instanceof Error error) throw error;
            throw e;
        } catch (ExceptionInInitializerError e) {
            if (e.getCause() instanceof ApplicationExitError exit) return exit.status();
            throw e;
        }
    }

    @Override
    public @NotNull CompletableFuture<Integer> start(@NotNull String... args) {
        CompletableFuture<Integer> result = new CompletableFuture<>();
        String[] arguments = args.clone();
        Thread thread = new Thread(() -> {
            try {
                result.complete(run(arguments));
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        }, "Plugin-" + loader.sandbox().pluginName() + "-Application-" + THREADS.incrementAndGet());
        // A forgotten application must not keep the server from shutting down
        thread.setDaemon(true);
        thread.start();
        return result;
    }

    /**
     * Finds {@code static void main(String[])}, or {@code static void main()}, in the application's own classes.
     */
    @NotNull
    private Method findMain() throws ClassNotFoundException, NoSuchMethodException {
        if (closed) throw new IllegalStateException("The application " + jar + " was closed");
        if (mainClass == null)
            throw new IllegalStateException("The application " + jar + " has no Main-Class, pass the main class when loading it");
        // Only the application's own classes, not e.g. JDK or API classes with a main method
        if (!loader.ownsClass(mainClass.replace('.', '/')))
            throw new ClassNotFoundException(mainClass + " is not a class of " + jar);

        Class<?> clazz = Class.forName(mainClass, true, loader);
        Method main;
        try {
            main = clazz.getDeclaredMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            main = clazz.getDeclaredMethod("main");
        }
        if (!Modifier.isStatic(main.getModifiers()) || main.getReturnType() != void.class)
            throw new NoSuchMethodException(mainClass + " has no static void main method");
        main.setAccessible(true);
        return main;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        closed = true;
        owner.removeApplication(this);
        loader.close();
    }
}
