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
        assert INSTANCE.impl != null;
        return INSTANCE.impl.getUtilityBackend();
    }

    /**
     * Shutdown the Bot.
     */
    public static void shutdown() {
        assert INSTANCE.impl != null;
        INSTANCE.impl.shutdown();
    }

    /**
     * Gets the Bot.
     *
     * @return The {@link Bot}.
     */
    @NotNull
    public static Bot getBot() {
        assert INSTANCE.impl != null;
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
        assert INSTANCE.impl != null;
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
        assert INSTANCE.impl != null;
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
        assert INSTANCE.impl != null;
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
        assert INSTANCE.impl != null;
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
        assert INSTANCE.impl != null;
        return INSTANCE.impl.getScheduler();
    }

    public void init(@NotNull InternalComitasImpl impl) {
        assert INSTANCE.impl == null;

        INSTANCE.impl = impl;

        INSTANCE.impl.init();
    }
}
