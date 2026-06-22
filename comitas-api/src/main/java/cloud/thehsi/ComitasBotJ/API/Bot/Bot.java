package cloud.thehsi.ComitasBotJ.API.Bot;

import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;

public class Bot {
    private static final Bot INSTANCE = new Bot();

    private InternalBotImpl impl;

    private Bot() {
    }

    public void init(InternalBotImpl impl) {
        INSTANCE.impl = impl;

        INSTANCE.impl.init(impl);
    }

    public static Bot getInstance() {
        return INSTANCE;
    }

    public static PluginManager getPluginManager() {
        return INSTANCE.impl.getPluginManager();
    }
}
