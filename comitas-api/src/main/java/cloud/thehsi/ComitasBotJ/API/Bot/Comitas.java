package cloud.thehsi.ComitasBotJ.API.Bot;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Scheduler;

@SuppressWarnings("unused")
public class Comitas {
    private static final Comitas INSTANCE = new Comitas();

    private InternalComitasImpl impl;

    private Comitas() {
    }

    /* Not supposed to be used by Plugin Developers */
    public static UtilityBackend getUtilityBackend() {
        return INSTANCE.impl.getUtilityBackend();
    }

    /**
     * Shutdown the Bot.
     */
    public static void shutdown() {
        INSTANCE.impl.shutdown();
    }

    /**
     * Gets the Bot.
     *
     * @return The {@link Bot}.
     */
    public static Bot getBot() {
        return INSTANCE.impl.getBot();
    }

    /**
     * Gets the instance of Comitas.
     * Should not be used
     *
     * @return Instance of {@link Comitas}.
     */
    public static Comitas getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the Server Version
     *
     * @return The Current Server Version.
     */
    public static String getServerVersion() {
        return INSTANCE.impl.getServerVersion();
    }

    /**
     * Gets the {@link PluginManager}.
     * <p>
     * The {@link PluginManager} is used to register events and interact with the Plugin Loader.
     *
     * @return The {@link PluginManager} in use by {@link Comitas}
     */
    public static PluginManager getPluginManager() {
        return INSTANCE.impl.getPluginManager();
    }

    /**
     * Gets the {@link CommandRegistry}.
     * <p>
     * The {@link CommandRegistry} is used to register Slash Commands.
     *
     * @return The {@link CommandRegistry} in use by {@link Comitas}
     */
    public static CommandRegistry getCommandRegistry() {
        return INSTANCE.impl.getCommandRegistry();
    }

    /**
     * Gets the {@link ConsoleCommandRegistry}.
     * <p>
     * The {@link ConsoleCommandRegistry} is used to register Console Commands.
     *
     * @return The {@link ConsoleCommandRegistry} in use by {@link Comitas}
     */
    public static ConsoleCommandRegistry getConsoleCommandRegistry() {
        return INSTANCE.impl.getConsoleCommandRegistry();
    }

    /**
     * Gets the {@link Scheduler}.
     * <p>
     * The {@link Scheduler} is used to run Tasks.
     *
     * @return The {@link Scheduler} in use by {@link Comitas}
     */
    public static Scheduler getScheduler() {
        return INSTANCE.impl.getScheduler();
    }

    public void init(InternalComitasImpl impl) {
        assert INSTANCE.impl == null;

        INSTANCE.impl = impl;

        INSTANCE.impl.init();
    }
}
