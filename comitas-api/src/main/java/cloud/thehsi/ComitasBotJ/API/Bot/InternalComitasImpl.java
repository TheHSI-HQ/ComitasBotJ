package cloud.thehsi.ComitasBotJ.API.Bot;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Scheduler;

public interface InternalComitasImpl {
    String getServerVersion();

    PluginManager getPluginManager();

    CommandRegistry getCommandRegistry();
    ConsoleCommandRegistry getConsoleCommandRegistry();

    Scheduler getScheduler();

    UtilityBackend getUtilityBackend();

    Bot getBot();

    void init();

    void shutdown();
}