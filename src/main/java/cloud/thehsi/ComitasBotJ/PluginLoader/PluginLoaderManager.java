package cloud.thehsi.ComitasBotJ.PluginLoader;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PluginLoaderManager {
    private final List<Plugin> plugins = new ArrayList<>();

    public Integer count() {
        return plugins.size();
    }

    public void loadPlugins() {
        File pluginDir = new File("plugins");
        File pluginDataDir = new File("plugin_data");

        if (!pluginDir.exists()) if (!pluginDir.mkdir()) throw new RuntimeException("Couldn't create plugins folder");
        if (!pluginDataDir.exists()) if (!pluginDataDir.mkdir()) throw new RuntimeException("Couldn't create plugin_data folder");

        File[] jars = pluginDir.listFiles(
                f -> f.getName().endsWith(".jar")
        );

        if (jars == null)
            return;

        for (File jar : jars) {

            try (URLClassLoader loader =
                    new URLClassLoader(
                            new URL[]{jar.toURI().toURL()},
                            getClass().getClassLoader()
                    )) {

                InputStream is =
                        loader.getResourceAsStream(
                                "plugin.properties"
                        );

                Properties props = new Properties();
                props.load(is);

                String mainClass =
                        props.getProperty("main");

                Class<?> clazz =
                        loader.loadClass(mainClass);

                Plugin plugin =
                        (Plugin) clazz.getDeclaredConstructor()
                                .newInstance();

                plugins.add(plugin);

                plugin.onEnable();

                System.out.println(
                        "Loaded: " + plugin.getName()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
