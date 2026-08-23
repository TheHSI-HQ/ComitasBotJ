package cloud.thehsi.ComitasBotJ.API.Bot;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;

public interface InternalComitasImpl {
    @NotNull String getServerVersion();

    @NotNull PluginManager getPluginManager();

    @NotNull CommandRegistry getCommandRegistry();

    @NotNull ConsoleCommandRegistry getConsoleCommandRegistry();

    @NotNull Scheduler getScheduler();

    @NotNull UtilityBackend getUtilityBackend();

    @NotNull Bot getBot();

    void init();

    void shutdown();
}