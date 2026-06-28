package cloud.thehsi.ComitasBotJ.API.Bot;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Scheduler;

import java.util.List;

public interface InternalComitasImpl {
    String getAPIVersion();

    String getServerVersion();

    PluginManager getPluginManager();
    ConsoleCommandRegistry getConsoleCommandRegistry();
    Scheduler getScheduler();

    List<Guild> getGuilds();

    void init();
}