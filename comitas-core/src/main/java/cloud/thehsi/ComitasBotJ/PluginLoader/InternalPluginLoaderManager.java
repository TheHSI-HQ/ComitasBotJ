package cloud.thehsi.ComitasBotJ.PluginLoader;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class InternalPluginLoaderManager implements InternalPluginLoaderManagerImpl {
    private final List<LoadedPlugin> plugins = new ArrayList<>();

    @Override
    public Integer count() {
        return plugins.size();
    }

    @Override
    public List<String> pluginNameList() {
        return plugins.stream().map(e -> e.name).toList();
    }

    @Override
    public void loadPlugins() {
        File pluginDir = new File("plugins");
        File pluginDataDir = new File("plugin_data");

        if (!pluginDir.exists()) if (!pluginDir.mkdir()) throw new RuntimeException("Couldn't create plugins folder");
        if (!pluginDataDir.exists())
            if (!pluginDataDir.mkdir()) throw new RuntimeException("Couldn't create plugin_data folder");

        File[] jars = pluginDir.listFiles(
                f -> f.getName().endsWith(".jar")
        );

        if (jars == null)
            return;

        for (File jar : jars) {
            try {
                URLClassLoader loader =
                         new URLClassLoader(
                                 new URL[]{jar.toURI().toURL()},
                                 getClass().getClassLoader()
                         );

                InputStream is =
                        loader.getResourceAsStream(
                                "plugin.properties"
                        );

                Properties props = new Properties();
                props.load(is);

                String mainClass =
                        props.getProperty("main");

                String name =
                        props.getProperty("name");

                Class<? extends Plugin> clazz =
                        loader.loadClass(mainClass)
                                .asSubclass(Plugin.class);

                Plugin plugin = clazz.getDeclaredConstructor()
                        .newInstance();

                plugins.add(new LoadedPlugin(plugin, loader, name));

                plugin.onEnable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unloadPlugins() {
        for (LoadedPlugin loaded : plugins) {
            try {
                loaded.plugin().onDisable();

                loaded.loader().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        plugins.clear();

        System.gc();
    }

    private record LoadedPlugin(Plugin plugin, URLClassLoader loader, String name) {
    }
}
