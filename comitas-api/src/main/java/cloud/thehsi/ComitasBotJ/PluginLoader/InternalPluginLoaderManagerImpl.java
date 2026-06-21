package cloud.thehsi.ComitasBotJ.PluginLoader;

import java.util.List;

public interface InternalPluginLoaderManagerImpl {
    Integer count();

    List<String> pluginNameList();

    void loadPlugins();
    void unloadPlugins();
}
