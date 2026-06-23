package cloud.thehsi.ComitasBotJ.API.Plugin;

import cloud.thehsi.ComitasBotJ.API.Event.Listener;

import java.util.List;

public interface InternalPluginManagerImpl {
    Integer countPlugins();

    List<String> getPluginNames();

    Plugin.PluginMetadata lookupPlugin(Plugin plugin);

    void reloadPlugins();

    void registerEvents(Plugin plugin, Listener listener);
}
