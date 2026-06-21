package cloud.thehsi.ComitasBotJ.Bot;

import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;

public interface InternalBotImpl {
    String getDiscordToken();

    PluginManager getPluginManager();

    void init(InternalBotImpl impl);
}