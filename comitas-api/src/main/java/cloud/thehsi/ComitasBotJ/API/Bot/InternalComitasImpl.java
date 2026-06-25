package cloud.thehsi.ComitasBotJ.API.Bot;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Scheduler;

public interface InternalComitasImpl {
    String getAPIVersion();

    String getServerVersion();

    PluginManager getPluginManager();
    ConsoleCommandRegistry getConsoleCommandRegistry();

    Scheduler getScheduler();

    void init();
}