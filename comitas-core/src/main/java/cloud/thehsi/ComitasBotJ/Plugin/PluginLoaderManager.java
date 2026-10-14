package cloud.thehsi.ComitasBotJ.Plugin;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Main;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.*;

@SuppressWarnings("unused")
public class PluginLoaderManager {
    private @NotNull
    final List<LoadedPlugin> plugins = new ArrayList<>();
    private @NotNull
    final Map<ClassLoader, Plugin> loaderToPluginCache = new HashMap<>();
    private @NotNull
    final Map<UUID, Plugin> uuidToPluginCache = new HashMap<>();
    private @NotNull
    final Logger debugLogger = DebugLogging.getLogger();
    private @NotNull
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".PluginLoader");
    public @Nullable InternalPluginManager pluginManager = null;
    @Nullable LoadedPlugin basePlugin = null;

    public PluginLoaderManager() {
    }

    public @Nullable Plugin getBasePlugin() {
        return basePlugin == null ?
                null : basePlugin.plugin();
    }

    public static long versionId(@NotNull String version) {
        version = version.trim().toLowerCase();

        int suffix = 0; // release
        char last = version.charAt(version.length() - 1);

        if (last == 'a') {
            suffix = -100;
            version = version.substring(0, version.length() - 1);
        } else if (last == 'b') {
            suffix = -99;
            version = version.substring(0, version.length() - 1);
        }

        String[] parts = version.split("\\.");

        int major = 0;
        int minor = 0;
        int patch = 0;

        try {
            if (parts.length > 0) major = Integer.parseInt(parts[0]);
            if (parts.length > 1) minor = Integer.parseInt(parts[1]);
            if (parts.length > 2) patch = Integer.parseInt(parts[2]);
        }catch (NumberFormatException e) {
            return 0;
        }

        return major * 1_000_000_000L
                + minor * 1_000_000L
                + patch * 1_000L
                + suffix;
    }

    public void initPluginManager(@NotNull InternalPluginManager pluginManager) {
        if (this.pluginManager == null) this.pluginManager = pluginManager;
    }

    public int count() {
        return plugins.size();
    }

    @NotNull
    public List<Plugin.PluginMetadata> pluginMetadataList() {
        return plugins.stream()
                .map(LoadedPlugin::metadata)
                .toList();
    }

    public void loadBasePlugin() {
        if (basePlugin == null)
            try {
                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Loading base plugin");
                ClassLoader loader = getClass().getClassLoader();

                Class<? extends Plugin> clazz =
                        Class.forName("cloud.thehsi.ComitasBasePlugin.Main", true, loader)
                                .asSubclass(Plugin.class);

                Plugin plugin = clazz.getDeclaredConstructor().newInstance();

                String jarName = new File(
                        Main.class
                                .getProtectionDomain()
                                .getCodeSource()
                                .getLocation()
                                .toURI()
                ).getName();

                basePlugin = new LoadedPlugin(plugin, null, new Plugin.PluginMetadata(
                        "Comitas", Main.getServerVersion(), jarName, Comitas.getServerVersion(), UUID.fromString("0000000-0000-0000-0000-000000000000"), "comitas")
                );

                plugins.add(basePlugin);

                plugin.onEnable();

            } catch (Exception e) {
                logger.error("Error loading base plugin: ", e);
                return;
            }

        if (!plugins.contains(basePlugin))
            plugins.add(basePlugin);
    }

    @Nullable
    public Plugin.PluginMetadata lookupPlugin(@NotNull Plugin plugin) {
        for (LoadedPlugin p : plugins) {
            if (p.plugin() == plugin) return p.metadata();
        }

        return null;
    }

    public void loadPlugins() {
        if (pluginManager == null)
            throw new IllegalStateException("Plugin Manager not initialized");

        if (!Main.props().strictSafeMode()) loadBasePlugin();

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Checking and creating plugins and plugin_data directories");

        File pluginDir = new File("plugins");
        File pluginDataDir = new File("plugin_data");

        if (!pluginDir.exists()) if (!pluginDir.mkdir()) throw new RuntimeException("Couldn't create plugins folder");
        if (!pluginDataDir.exists())
            if (!pluginDataDir.mkdir()) throw new RuntimeException("Couldn't create plugin_data folder");

        if (Main.props().strictSafeMode() || Main.props().safeMode() || !Main.conf().loadPlugins.get()) return;

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Listing all jar files from plugins");
        File[] jars = pluginDir.listFiles(
                f -> f.getName().endsWith(".jar")
        );

        if (jars == null)
            return;

        List<String> allowedPlugins = new ArrayList<>();
        boolean nonWhitelistedFound = false;

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Parsing plugin whitelist");
        if (!Objects.equals(Main.conf().allowedPlugins.get(), "*"))
            allowedPlugins = List.of(Main.conf().allowedPlugins.get().split(","));

        for (File jar : jars) {
            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Loading Plugin Jar File: {}", jar.getName());
            try {
                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Creating URLClassLoader for: {}", jar.getName());
                URLClassLoader loader =
                        new URLClassLoader(
                                new URL[]{jar.toURI().toURL()},
                                getClass().getClassLoader()
                        );

                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Reading plugin.properties of: {}", jar.getName());
                InputStream is =
                        loader.getResourceAsStream(
                                "plugin.properties"
                        );

                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Parsing plugin.properties for: {}", jar.getName());
                Properties props = new Properties();
                props.load(is);
                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Parsed plugin.properties: {}", props);

                String mainClass =
                        props.getProperty("main");

                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Prepping internal plugin metadata for: {}", jar.getName());
                Plugin.PluginMetadata metadata = Plugin.PluginMetadata.fromProperties(
                        props, jar.getName()
                );

                if (!allowedPlugins.isEmpty()) {
                    boolean found = false;
                    boolean hashValid = true;
                    String hash = "";
                    String original_hash = "";
                    for (String allowedPlugin : allowedPlugins) {
                        if (allowedPlugin.equals(metadata.name()) || allowedPlugin.equals(metadata.uuid().toString())) {
                            found = true;
                            break;
                        }

                        if (allowedPlugin.toLowerCase().split("!sha256=").length < 2)
                            continue;

                        String raw = allowedPlugin.split("!sha256=")[0];
                        original_hash = allowedPlugin.split("!sha256=")[1];
                        if (raw.equals(metadata.name()) || raw.equals(metadata.uuid().toString())) {
                            found = true;
                            try (InputStream jar_is = Files.newInputStream(jar.toPath())) {
                                hash = DigestUtils.sha256Hex(jar_is);
                                hashValid = Objects.equals(original_hash, hash);
                            }
                            break;
                        }
                    }

                    if (!nonWhitelistedFound && !found)
                        logger.warn("""
                                {}
                                ╔══════════════════════════════════════════════════════════════════╗
                                ║                       PLUGIN SECURITY WARNING                    ║
                                ╚══════════════════════════════════════════════════════════════════╝
                                
                                Plugin whitelist enforcement is enabled, and one or more plugins
                                in the plugins directory are not whitelisted. These plugins will
                                NOT be loaded.
                                
                                If you did not intentionally place these plugins here, treat this
                                as a potential security issue. Review your access logs, deployment
                                history, filesystem changes, and other relevant audit logs to
                                determine how they got there.
                                
                                Do NOT leave unrecognized or unwhitelisted plugins in the plugins
                                directory in a production environment. Remove them and investigate
                                their origin before continuing.
                                
                                Whitelist only plugins that you explicitly trust and intend to run.
                                
                                For production use, it is recommended to also validate the file hash,
                                by adding !sha256= to the end of the uuid or plugin name and
                                prepending the hash afterwards.
                                
                                """, ConsoleColor.YELLOW);

                    if (!found)
                        logger.warn("Plugin '{}' ({}) is not whitelisted and will be skipped because the plugin whitelist is enabled.",
                                metadata.name(), metadata.jarName());
                    if (found && !hashValid)
                        logger.warn("Plugin '{}' ({}) is whitelisted but its hash does not match the specified one:\n  Old: {} != New: {}.",
                                metadata.name(), metadata.jarName(), original_hash, hash);
                    if (!found || !hashValid) {
                        nonWhitelistedFound = true;
                        continue;
                    }
                }

                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Validating UUID ({}) for: {}", props.getProperty("uuid"), jar.getName());
                if (!Objects.equals(props.getProperty("uuid"), metadata.uuid().toString()))
                    logger.warn("Plugin {} is not using universal UUID formatting", metadata.name());

                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Validating name ({}) for: {}", metadata.name(), jar.getName());
                if (metadata.name().contains(","))
                    logger.warn("Plugin {} contains an illegal character in its name [,]", metadata.name());

                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Checking compatibility ({}) for: {}", props.getProperty("api-target"), jar.getName());
                if (!isApiTargetCompatible(props.getProperty("api-target")))
                    if (Main.props().ignoreApiTarget())
                        logger.warn("Plugin only supports {}, current version is {}", props.getProperty("api-target"), Comitas.getServerVersion());
                    else
                        throw new PluginCompatibilityException(
                                props.getProperty("api-target"), Comitas.getServerVersion()
                        );

                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Reflecting main class for: {}", jar.getName());
                Class<? extends Plugin> clazz =
                        loader.loadClass(mainClass)
                                .asSubclass(Plugin.class);

                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Instancing main class for : {}", jar.getName());
                Plugin plugin = clazz.getDeclaredConstructor()
                        .newInstance();

                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Adding {} as a loaded plugin", jar.getName());
                plugins.add(new LoadedPlugin(plugin, loader, metadata));

                loaderToPluginCache.put(loader, plugin);
                uuidToPluginCache.put(metadata.uuid(), plugin);

                logger.info("Loaded Plugin {} {}", metadata.name(), metadata.version());

                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Loading datastore (plugin data) for: {}", jar.getName());
                pluginManager.loadDataStore(metadata.uuid());

                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Calling onEnable on: {}", jar.getName());
                plugin.onEnable();
            } catch (PluginCompatibilityException e) {
                logger.error("Incompatible plugin found: \"{}\":", jar.getName());
                logger.error("{}[{}]{} {}",
                        ConsoleColor.BRIGHT_BLACK,
                        ConsoleColor.BRIGHT_RED + jar.getName().replaceFirst(".jar$", "") + ConsoleColor.BRIGHT_BLACK,
                        ConsoleColor.WHITE,
                        e.getLocalizedMessage()
                );
            } catch (Exception e) {
                logger.error("Error when loading: \"{}\":", jar.getName());
                logger.error("{}[{}]{} {}",
                        ConsoleColor.BRIGHT_BLACK,
                        ConsoleColor.BRIGHT_BLUE + jar.getName().replaceFirst(".jar$", "") + ConsoleColor.BRIGHT_BLACK,
                        ConsoleColor.WHITE,
                        e.getLocalizedMessage(), e
                );
            }
        }
    }

    private boolean isApiTargetCompatible(@NotNull String target) {
        long apiVersion = versionId(Comitas.getServerVersion());

        target = target.trim();
        String[] parts = target.split("-");

        if (parts.length == 1) {
            return apiVersion == versionId(parts[0]);
        }

        return versionId(parts[0]) <= apiVersion && apiVersion <= versionId(parts[1]);
    }

    private boolean isApiTargetCompatible(@NotNull String target, @NotNull String overwriteApiVersion) {
        long apiVersion = versionId(overwriteApiVersion);

        target = target.trim();
        String[] parts = target.split("-");

        if (parts.length == 1) {
            return apiVersion == versionId(parts[0]);
        }

        return versionId(parts[0]) <= apiVersion && apiVersion <= versionId(parts[1]);
    }

    public void unloadPlugins() {
        if (pluginManager == null)
            throw new IllegalStateException("Plugin Manager not initialized");

        for (LoadedPlugin loaded : plugins) {
            try {
                loaded.plugin().onDisable();

                pluginManager.saveDataStore(loaded.metadata().uuid());

                loaderToPluginCache.remove(loaded.loader);
                uuidToPluginCache.remove(loaded.metadata().uuid());

                if (loaded.loader != null)
                    loaded.loader().close();
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }

        plugins.clear();

        System.gc();
    }

    @Nullable
    public Plugin getPlugin(@NotNull ClassLoader classLoader) {
        return loaderToPluginCache.get(classLoader);  // O(1) lookup
    }

    @Nullable
    public Plugin getPlugin(@NotNull UUID uuid) {
        return uuidToPluginCache.get(uuid);  // O(1) lookup
    }

    private record LoadedPlugin(Plugin plugin, @Nullable URLClassLoader loader, Plugin.PluginMetadata metadata) {
    }
}
