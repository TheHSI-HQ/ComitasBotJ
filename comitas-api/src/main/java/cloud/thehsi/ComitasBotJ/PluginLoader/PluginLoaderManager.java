package cloud.thehsi.ComitasBotJ.PluginLoader;

public class PluginLoaderManager {
    private final InternalPluginLoaderManagerImpl impl;

    public PluginLoaderManager(InternalPluginLoaderManagerImpl impl) {
        this.impl = impl;
    }

    public Integer count() {
        return impl.count();
    }

    public void loadPlugins() {
        impl.loadPlugins();
    }

    public void unloadPlugins() {
        impl.unloadPlugins();
    }
}
