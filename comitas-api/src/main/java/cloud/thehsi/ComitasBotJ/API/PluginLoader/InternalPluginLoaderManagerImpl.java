package cloud.thehsi.ComitasBotJ.API.PluginLoader;

import java.util.List;

public interface InternalPluginLoaderManagerImpl {
    Integer count();

    List<String> pluginNameList();

    void loadPlugins();
    void unloadPlugins();
}
