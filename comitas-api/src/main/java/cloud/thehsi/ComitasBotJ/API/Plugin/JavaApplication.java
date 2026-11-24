package cloud.thehsi.ComitasBotJ.API.Plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * A jar, loaded by a plugin with {@link PluginPermission#JAVA_APPLICATIONS}, see {@link Plugin#loadJavaApplication(Path)}.
 * <p>
 * The application runs inside the plugin's sandbox: it is verified like the plugin's own classes and only has the
 * permissions the plugin was granted. It sees the plugin's file system ({@code Path.of} resolves in it), its console
 * output goes to the plugin's logger and API calls it makes are made on behalf of the plugin.
 * <p>
 * The application has its own class loader, its classes are not visible to the plugin (and the other way round).
 * Jars listed in the {@code Class-Path} of its manifest are loaded as well, if they exist in the plugin's file system.
 * <p>
 * {@code System.exit(status)} doesn't stop the server, it ends the application's main method with that status.
 */
@SuppressWarnings("unused")
public interface JavaApplication extends AutoCloseable {
    /**
     * @return The jar in the plugin's file system
     */
    @NotNull
    Path getJar();

    /**
     * @return The class, whose {@code main} method is run: the one given when loading, otherwise the
     * {@code Main-Class} of the jar's manifest. Null, if there is neither.
     */
    @Nullable
    String getMainClass();

    /**
     * Runs the application's {@code main} method on the current thread and waits until it returns.
     * <p>
     * Don't call this from a console command or event handler, if the application runs for long, use {@link #start(String...)}.
     *
     * @param args The command line arguments
     * @return The exit status: 0 if {@code main} returned, otherwise the status passed to {@code System.exit}
     * @throws IllegalStateException If the application has no main class or was closed
     * @throws Exception             What the application's {@code main} method threw
     */
    int run(@NotNull String... args) throws Exception;

    /**
     * Runs the application's {@code main} method on a new thread, see {@link #run(String...)}.
     *
     * @param args The command line arguments
     * @return A future completing with the exit status, or exceptionally with what {@code main} threw
     */
    @NotNull
    CompletableFuture<Integer> start(@NotNull String... args);

    /**
     * @return True, if {@link #close()} was called (or the plugin was unloaded)
     */
    boolean isClosed();

    /**
     * Closes the application's class loader, no new classes can be loaded from its jars afterward.
     * Threads the application started keep running, until they end on their own.
     * <p>
     * Applications are closed automatically, when the plugin is unloaded.
     */
    @Override
    void close() throws IOException;
}
