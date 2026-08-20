package cloud.thehsi.ComitasBotJ.Plugin;

import cloud.thehsi.ComitasBotJ.API.Event.Listener;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataStorage;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Commands.InternalCommandRegistry;
import cloud.thehsi.ComitasBotJ.Discord.DiscordAPI;
import cloud.thehsi.ComitasBotJ.Event.EventManager;
import cloud.thehsi.ComitasBotJ.Event.Events.InternalBotReadyEvent;
import cloud.thehsi.ComitasBotJ.Main;
import cloud.thehsi.ComitasBotJ.Plugin.PersistentData.InternalPersistentDataStorage;
import cloud.thehsi.ComitasBotJ.Plugin.PersistentData.PersistentDataSerializer;
import cloud.thehsi.ComitasBotJ.Scheduler.InternalScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InternalPluginManager implements PluginManager {
    private static final StackWalker STACK_WALKER =
            StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private final PluginLoaderManager pluginLoaderManager;
    private final EventManager eventManager;
    private final InternalScheduler scheduler;
    private final InternalCommandRegistry commandRegistry;
    private final Map<UUID, InternalPersistentDataStorage> pluginDataStores = new HashMap<>();
    private final Logger debugLogger;
    private final Logger logger;
    private DiscordAPI discordAPI;

    public InternalPluginManager(PluginLoaderManager pluginLoaderManager, EventManager eventManager, InternalScheduler scheduler, InternalCommandRegistry commandRegistry) {
        this.pluginLoaderManager = pluginLoaderManager;
        this.eventManager = eventManager;
        this.scheduler = scheduler;
        this.commandRegistry = commandRegistry;
        this.debugLogger = DebugLogging.getLogger();
        this.logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".PluginManager");

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Populating Plugin Loader Manager");
        this.pluginLoaderManager.initPluginManager(this);

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Starting Plugin Data Auto Save on 5 Minute interval");
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(() -> {
            for (Plugin.PluginMetadata metadata : getAllPluginMetadata())
                saveDataStore(metadata.uuid());
        }, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public Integer countPlugins() {
        DebugLogging.action();
        return pluginLoaderManager.count();
    }

    @Override
    public List<Plugin.PluginMetadata> getAllPluginMetadata() {
        DebugLogging.action();
        return pluginLoaderManager.pluginMetadataList();
    }

    @Override
    public Plugin getPlugin() {
        DebugLogging.action();
        if (DebugLogging.isActionEnabled()) debugLogger.debug("Fetching calling plugin...");
        return STACK_WALKER
                .walk(frames -> frames
                        .map(StackWalker.StackFrame::getDeclaringClass)
                        .map(Class::getClassLoader)
                        .map(pluginLoaderManager::getPlugin)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null)
                );
    }

    @Override
    public PersistentDataStorage getPersistentDataStorage() {
        DebugLogging.action();
        Plugin plugin = getPlugin();
        Plugin.PluginMetadata metadata = lookupPlugin(plugin);
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("{} requested persistant data storage.", metadata.name());

        populateDataStore(metadata.uuid());

        return pluginDataStores.get(metadata.uuid());
    }

    public void populateDataStore(UUID pluginUUID) {
        if (pluginDataStores.containsKey(pluginUUID))
            return;
        pluginDataStores.put(pluginUUID, new InternalPersistentDataStorage());
    }

    public void loadDataStore(UUID pluginUUID) {
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Loading data stores for {}.", pluginUUID);
        Path dataDirectory = Path.of("plugin_data");
        Path dataFile = dataDirectory.resolve(pluginUUID + ".dat");

        try {
            if (DebugLogging.isBasicEnabled())//noinspection LoggingSimilarMessage
                debugLogger.debug("Creating plugin_data directory if it doesn't exist.");
            Files.createDirectories(dataDirectory);

            if (Files.notExists(dataFile)) {
                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Initializing empty data store for {}, as no ore-existing one was found.", pluginUUID);
                pluginDataStores.put(
                        pluginUUID,
                        new InternalPersistentDataStorage()
                );

                return;
            }

            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Reading raw data store file of {}.", pluginUUID);
            byte[] serializedData = Files.readAllBytes(dataFile);

            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Parsing read data into data store for {}.", pluginUUID);
            Map<String, InternalPersistentDataStorage.Entry> data =
                    PersistentDataSerializer.deserializeData(
                            serializedData
                    );

            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Registering data store for {}.", pluginUUID);
            pluginDataStores.put(
                    pluginUUID,
                    new InternalPersistentDataStorage(data)
            );

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load plugin data store for "
                            + pluginUUID,
                    e
            );
        }
    }

    public void saveDataStore(UUID pluginUUID) {
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Saving data store to disk for {}.", pluginUUID);
        InternalPersistentDataStorage dataStore =
                pluginDataStores.get(pluginUUID);

        if (dataStore == null) {
            if (DebugLogging.isBasicEnabled()) debugLogger.debug("No data store found for {}, skipping.", pluginUUID);
            return;
        }

        Path directory = Path.of("plugin_data");
        Path target = directory.resolve(
                pluginUUID + ".dat"
        );
        Path temporary = directory.resolve(
                pluginUUID + ".dat.tmp"
        );

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Serializing data store of {}.", pluginUUID);
        byte[] data =
                PersistentDataSerializer.serializeData(
                        dataStore.getData()
                ).getBytes();

        int attempts = 1;

        while (true) {
            try {
                if (DebugLogging.isBasicEnabled())//noinspection LoggingSimilarMessage
                    debugLogger.debug("Creating plugin_data directory if it doesn't exist.");
                Files.createDirectories(directory);

                // Write the complete data first
                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Writing data store into temporary file for {}.", pluginUUID);
                Files.write(
                        temporary,
                        data
                );

                // Only replace the real file after the write succeeds
                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Overwriting data store file for {}.", pluginUUID);
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

                if (attempts > 1)
                    logger.info(
                            "Plugin data for {} was saved (After {} attempts). ",
                            pluginUUID,
                            attempts
                    );

                return;
            } catch (IOException e) {
                attempts++;
                logger.error(
                        "CRITICAL: Failed to save plugin data for {}. " +
                                "The server will continue retrying until the data " +
                                "can be safely saved. (Attempt {})",
                        pluginUUID,
                        attempts,
                        e
                );
            }

            try {
                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Waiting 1s to retry saving data for {}.", pluginUUID);
                //noinspection BusyWait
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                logger.error(
                        "CRITICAL: Save operation interrupted for {}. " +
                                "Data may be lost!",
                        pluginUUID
                );

                return;
            }
        }
    }

    @Override
    public Plugin.PluginMetadata lookupPlugin(Plugin plugin) {
        DebugLogging.action(plugin);
        return pluginLoaderManager.lookupPlugin(plugin);
    }

    private void unloadPlugins(boolean hard) {
        pluginLoaderManager.unloadPlugins();
        if (hard) {
            logger.info("Unregistering all commands...");
            commandRegistry.unregisterAll();
        }
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Canceling all Schedulers");
        scheduler.cancelAll();
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Clearing all Events");
        eventManager.clearEventListeners();
    }

    @Override
    public void reloadSoft() {
        DebugLogging.action();
        long reloadTime = System.currentTimeMillis();
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Unloading Plugins in soft mode");
        unloadPlugins(false);
        if (DebugLogging.isBasicEnabled())//noinspection LoggingSimilarMessage
            debugLogger.debug("Loading Plugins");
        pluginLoaderManager.loadPlugins();

        // Fake the bot being ready, so plugins listening for it can react
        if (discordAPI != null) {
            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Faking and Firing a BotReadyEvent");
            eventManager.callEvent(new InternalBotReadyEvent(discordAPI.getAPI().getSelfUser()));
        }
        logger.info("Reloaded (Soft) in {}s", (System.currentTimeMillis() - reloadTime) / 1000d);
    }

    @Override
    public void reloadHard() {
        DebugLogging.action();
        long reloadTime = System.currentTimeMillis();
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Unloading Plugins in hard mode");
        unloadPlugins(true);

        try {
            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Reloading Config");
            Main.conf().load();
        } catch (IOException e) {
            logger.warn("Error during config reload: ", e);
            logger.warn("Old config will be preserved");
        }
        try {
            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Rewriting Config");
            Main.conf().save();
        } catch (IOException e) {
            logger.warn("Error during config save: ", e);
        }


        if (DebugLogging.isBasicEnabled())//noinspection LoggingSimilarMessage
            debugLogger.debug("Loading Plugins");
        pluginLoaderManager.loadPlugins();

        try {
            if (discordAPI != null) {
                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Reconnecting DiscordAPI");
                discordAPI.reconnect();
            }

        } catch (InterruptedException e) {
            throw new RuntimeException("Reload aborted");
        }

        logger.info("Reloaded (Hard) in {}s", (System.currentTimeMillis() - reloadTime) / 1000d);
    }

    @Override
    public void registerEvents(Plugin plugin, Listener listener) {
        DebugLogging.action(plugin, listener);
        eventManager.registerListener(plugin, listener);
    }

    public void setDiscordApi(DiscordAPI api) {
        discordAPI = api;
    }
}

