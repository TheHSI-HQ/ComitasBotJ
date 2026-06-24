package cloud.thehsi.ComitasBotJ.API.Bot;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;

public interface InternalComitasImpl {
    String getAPIVersion();

    String getServerVersion();

    PluginManager getPluginManager();
    ConsoleCommandRegistry getConsoleCommandRegistry();

    void init(InternalComitasImpl impl);
}