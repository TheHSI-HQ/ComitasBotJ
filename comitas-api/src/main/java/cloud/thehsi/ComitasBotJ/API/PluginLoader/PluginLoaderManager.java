package cloud.thehsi.ComitasBotJ.API.PluginLoader;

import java.util.List;

public class PluginLoaderManager {
    private final InternalPluginLoaderManagerImpl impl;

    public PluginLoaderManager(InternalPluginLoaderManagerImpl impl) {
        this.impl = impl;
    }

    public Integer count() {
        return impl.count();
    }

    public List<String> pluginNameList() {
        return impl.pluginNameList();
    }

    public void loadPlugins() {
        impl.loadPlugins();
    }

    public void unloadPlugins() {
        impl.unloadPlugins();
    }
}
