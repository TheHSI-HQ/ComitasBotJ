package cloud.thehsi.ComitasBotJ.API.Bot;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Scheduler;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Internal Implementation of Comitas
 * <p>
 * NOT TO BE USED IN PLUGINS
 *
 * @hidden
 */
@SuppressWarnings("doclint:missing")
@ApiStatus.Internal
public interface InternalComitasImpl {
    /**
     * @return The Server Version
     * @hidden
     * @see Comitas
     */
    @ApiStatus.Internal
    @NotNull String getServerVersion();

    /**
     * @return The Plugin Manager
     * @hidden
     * @see Comitas
     */
    @ApiStatus.Internal
    @NotNull PluginManager getPluginManager();

    /**
     * @return The Command Registry
     * @hidden
     * @see Comitas
     */
    @ApiStatus.Internal
    @NotNull CommandRegistry getCommandRegistry();

    /**
     * @return The Console Command Registry
     * @hidden
     * @see Comitas
     */
    @ApiStatus.Internal
    @NotNull ConsoleCommandRegistry getConsoleCommandRegistry();

    /**
     * @return The Scheduler
     * @hidden
     * @see Comitas
     */
    @ApiStatus.Internal
    @NotNull Scheduler getScheduler();

    /**
     * @return The Utility Backend
     * @hidden
     * @see Comitas
     */
    @ApiStatus.Internal
    @NotNull UtilityBackend getUtilityBackend();

    /**
     * @return The Bot
     * @hidden
     * @see Comitas
     */
    @ApiStatus.Internal
    @NotNull Bot getBot();

    /**
     * @hidden
     * @see Comitas
     */
    @ApiStatus.Internal
    void init();

    /**
     * @hidden
     * @see Comitas
     */
    @ApiStatus.Internal
    void shutdown();
}