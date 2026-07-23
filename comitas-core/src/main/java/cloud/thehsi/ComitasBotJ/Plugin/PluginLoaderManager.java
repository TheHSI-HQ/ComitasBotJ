package cloud.thehsi.ComitasBotJ.Plugin;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

@SuppressWarnings("unused")
public class PluginLoaderManager {
    private final List<LoadedPlugin> plugins = new ArrayList<>();
    public InternalPluginManager pluginManager = null;

    private final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".PluginLoader");

    public PluginLoaderManager() {
    }

    public void initPluginManager(InternalPluginManager pluginManager) {
        if (this.pluginManager == null) this.pluginManager = pluginManager;
    }

    public Integer count() {
        return plugins.size();
    }

    public List<Plugin.PluginMetadata> pluginMetadataList() {
        List<Plugin.PluginMetadata> metadataList = new ArrayList<>();
        for (LoadedPlugin plugin : plugins)
            metadataList.add(plugin.metadata());
        return metadataList;
    }

    public Plugin.PluginMetadata lookupPlugin(Plugin plugin) {
        for (LoadedPlugin p : plugins) {
            if (p.plugin() == plugin) return p.metadata();
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

            plugins.add(new LoadedPlugin(plugin, null, new Plugin.PluginMetadata(
                    "Base", "0.1b", Comitas.getAPIVersion(), UUID.fromString("7fe3a14c-69f5-49a9-a6eb-7321311b7864"), "comitas")
            ));

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

                Class<? extends Plugin> clazz =
                        loader.loadClass(mainClass)
                                .asSubclass(Plugin.class);

                Plugin plugin = clazz.getDeclaredConstructor()
                        .newInstance();

                Plugin.PluginMetadata metadata = Plugin.PluginMetadata.fromProperties(
                        props
                );

                if (!Objects.equals(props.getProperty("uuid"), metadata.uuid().toString()))
                    logger.warn("Plugin {} is not using universal UUID formatting", name);

                if (!isApiTargetCompatible(props.getProperty("api-target")))
                    throw new RuntimeException(
                            "API Version Range " +
                                    props.getProperty("api-target") +
                                    " is incompatible with " +
                                    Comitas.getAPIVersion()
                    );

                plugins.add(new LoadedPlugin(plugin, loader, metadata));

                logger.info("Loaded Plugin {} {}", name, version);

                pluginManager.loadDataStore(metadata.uuid());

                plugin.onEnable();
            } catch (Exception e) {
                logger.error("Error when loading: \"{}\":", jar.getName());
                logger.error("{}[{}]{} {}",
                        ConsoleColor.BRIGHT_BLACK,
                        ConsoleColor.BLUE + jar.getName().replaceFirst(".jar$", "") + ConsoleColor.BRIGHT_BLACK,
                        ConsoleColor.WHITE,
                        e.getLocalizedMessage()
                );
            }
        }
    }

    private long versionId(String version) {
        version = version.trim().toLowerCase();

        int suffix = 2; // release
        char last = version.charAt(version.length() - 1);

        if (last == 'a') {
            suffix = 0;
            version = version.substring(0, version.length() - 1);
        } else if (last == 'b') {
            suffix = 1;
            version = version.substring(0, version.length() - 1);
        }

        String[] parts = version.split("\\.");

        int major = 0;
        int minor = 0;
        int patch = 0;

        if (parts.length > 0) major = Integer.parseInt(parts[0]);
        if (parts.length > 1) minor = Integer.parseInt(parts[1]);
        if (parts.length > 2) patch = Integer.parseInt(parts[2]);

        return major * 1_000_000_000L
                + minor * 1_000_000L
                + patch * 1_000L
                + suffix;
    }

    private boolean isApiTargetCompatible(String target) {
        long apiVersion = versionId(Comitas.getAPIVersion());

        target = target.trim();
        String[] parts = target.split("-");

        if (parts.length == 1) {
            return apiVersion == versionId(parts[0]);
        }

        return versionId(parts[0]) <= apiVersion && apiVersion <= versionId(parts[1]);
    }

    public void unloadPlugins() {
        for (LoadedPlugin loaded : plugins) {
            try {
                loaded.plugin().onDisable();

                pluginManager.saveDataStore(loaded.metadata().uuid());

                if (loaded.loader != null)
                    loaded.loader().close();
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }

        plugins.clear();

        System.gc();
    }

    public Plugin getPlugin(ClassLoader classLoader) {
        for (LoadedPlugin plugin : plugins) {
            if (plugin.loader == classLoader) return plugin.plugin;
        }
        return null;
    }

    public Plugin getPlugin(UUID uuid) {
        for (LoadedPlugin plugin : plugins) {
            if (Objects.equals(plugin.metadata.uuid(), uuid)) return plugin.plugin;
        }
        return null;
    }

    private record LoadedPlugin(Plugin plugin, URLClassLoader loader, Plugin.PluginMetadata metadata) {
    }
}
