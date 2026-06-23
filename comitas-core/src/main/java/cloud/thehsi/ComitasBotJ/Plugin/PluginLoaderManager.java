package cloud.thehsi.ComitasBotJ.Plugin;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("unused")
public class PluginLoaderManager {
    private final List<LoadedPlugin> plugins = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger("ComitasBotJ.PluginLoader");

    public PluginLoaderManager() {
    }

    public Integer count() {
        return plugins.size();
    }

    public List<String> pluginNameList() {
        return plugins.stream().map(e -> e.name).toList();
    }

    public Plugin.PluginMetadata lookupPlugin(Plugin plugin) {
        for (LoadedPlugin p : plugins) {
            if (p.plugin() == plugin) return new Plugin.PluginMetadata(p.name(), p.version(), p.consoleCommandPrefix());
        }

        return null;
    }

    boolean isBasePluginLoaded = false;

    public void loadBasePlugin() {
        if (isBasePluginLoaded) return;
        isBasePluginLoaded = true;
        try {
            ClassLoader loader = getClass().getClassLoader();

            Class<? extends Plugin> clazz =
                    Class.forName("cloud.thehsi.ComitasBasePlugin.Main", true, loader)
                            .asSubclass(Plugin.class);

            Plugin plugin = clazz.getDeclaredConstructor().newInstance();

            plugins.add(new LoadedPlugin(plugin, null, "BasePlugin", "0.1b", "comitas"));

            plugin.onEnable();

        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    public void loadPlugins() {
        loadBasePlugin();

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

                String version =
                        props.getProperty("version");

                String consoleCommandPrefix =
                        props.getProperty("command-refix", name);


                Class<? extends Plugin> clazz =
                        loader.loadClass(mainClass)
                                .asSubclass(Plugin.class);

                Plugin plugin = clazz.getDeclaredConstructor()
                        .newInstance();

                plugins.add(new LoadedPlugin(plugin, loader, name, version, consoleCommandPrefix));

                logger.info("Loaded Plugin {} {}", name, version);

                plugin.onEnable();
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }

    public void unloadPlugins() {
        for (LoadedPlugin loaded : plugins) {
            try {
                loaded.plugin().onDisable();

                if (loaded.loader != null)
                    loaded.loader().close();
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }

        plugins.clear();

        System.gc();
    }

    private record LoadedPlugin(Plugin plugin, URLClassLoader loader, String name, String version,
                                String consoleCommandPrefix) {
    }
}
