package cloud.thehsi.ComitasBotJ.API.Bot;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class Comitas {
    private @NotNull
    static final Comitas INSTANCE = new Comitas();

    private @Nullable InternalComitasImpl impl;

    private Comitas() {
    }

    /* Not supposed to be used by Plugin Developers */
    @NotNull
    public static UtilityBackend getUtilityBackend() {
        if (INSTANCE.impl == null)
            throw new IllegalStateException("Intance not initialized");
        return INSTANCE.impl.getUtilityBackend();
    }

    /**
     * Shutdown the Bot.
     */
    public static void shutdown() {
        if (INSTANCE.impl == null)
            throw new IllegalStateException("Intance not initialized");
        INSTANCE.impl.shutdown();
    }

    /**
     * Gets the Bot.
     *
     * @return The {@link Bot}.
     */
    @NotNull
    public static Bot getBot() {
        if (INSTANCE.impl == null)
            throw new IllegalStateException("Intance not initialized");
        return INSTANCE.impl.getBot();
    }

    /**
     * Gets the instance of Comitas.
     * Should not be used
     *
     * @return Instance of {@link Comitas}.
     */
    @NotNull
    public static Comitas getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the Server Version
     *
     * @return The Current Server Version.
     */
    @NotNull
    public static String getServerVersion() {
        if (INSTANCE.impl == null)
            throw new IllegalStateException("Intance not initialized");
        return INSTANCE.impl.getServerVersion();
    }

    /**
     * Gets the {@link PluginManager}.
     * <p>
     * The {@link PluginManager} is used to register events and interact with the Plugin Loader.
     *
     * @return The {@link PluginManager} in use by {@link Comitas}
     */
    @NotNull
    public static PluginManager getPluginManager() {
        if (INSTANCE.impl == null)
            throw new IllegalStateException("Intance not initialized");
        return INSTANCE.impl.getPluginManager();
    }

    /**
     * Gets the {@link CommandRegistry}.
     * <p>
     * The {@link CommandRegistry} is used to register Slash Commands.
     *
     * @return The {@link CommandRegistry} in use by {@link Comitas}
     */
    @NotNull
    public static CommandRegistry getCommandRegistry() {
        if (INSTANCE.impl == null)
            throw new IllegalStateException("Intance not initialized");
        return INSTANCE.impl.getCommandRegistry();
    }

    /**
     * Gets the {@link ConsoleCommandRegistry}.
     * <p>
     * The {@link ConsoleCommandRegistry} is used to register Console Commands.
     *
     * @return The {@link ConsoleCommandRegistry} in use by {@link Comitas}
     */
    @NotNull
    public static ConsoleCommandRegistry getConsoleCommandRegistry() {
        if (INSTANCE.impl == null)
            throw new IllegalStateException("Intance not initialized");
        return INSTANCE.impl.getConsoleCommandRegistry();
    }

    /**
     * Gets the {@link Scheduler}.
     * <p>
     * The {@link Scheduler} is used to run Tasks.
     *
     * @return The {@link Scheduler} in use by {@link Comitas}
     */
    @NotNull
    public static Scheduler getScheduler() {
        if (INSTANCE.impl == null)
            throw new IllegalStateException("Intance not initialized");
        return INSTANCE.impl.getScheduler();
    }

    public void init(@NotNull InternalComitasImpl impl) {
        if (INSTANCE.impl != null)
            throw new IllegalStateException("Intance already initialized");

        INSTANCE.impl = impl;

        INSTANCE.impl.init();
    }
}
