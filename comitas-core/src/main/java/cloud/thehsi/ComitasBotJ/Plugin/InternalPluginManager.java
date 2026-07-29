package cloud.thehsi.ComitasBotJ.Plugin;

import cloud.thehsi.ComitasBotJ.API.Event.Listener;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataStorage;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
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
    private final Logger logger;
    private DiscordAPI discordAPI;

    public InternalPluginManager(PluginLoaderManager pluginLoaderManager, EventManager eventManager, InternalScheduler scheduler, InternalCommandRegistry commandRegistry) {
        this.pluginLoaderManager = pluginLoaderManager;
        this.eventManager = eventManager;
        this.scheduler = scheduler;
        this.commandRegistry = commandRegistry;

        this.pluginLoaderManager.initPluginManager(this);
        this.logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".PluginManager");

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(() -> {
            for (Plugin.PluginMetadata metadata : getAllPluginMetadata())
                saveDataStore(metadata.uuid());
        }, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public Integer countPlugins() {
        return pluginLoaderManager.count();
    }

    @Override
    public List<Plugin.PluginMetadata> getAllPluginMetadata() {
        return pluginLoaderManager.pluginMetadataList();
    }

    @Override
    public Plugin getPlugin() {
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
        Plugin plugin = getPlugin();
        Plugin.PluginMetadata metadata = lookupPlugin(plugin);

        populateDataStore(metadata.uuid());

        return pluginDataStores.get(metadata.uuid());
    }

    public void populateDataStore(UUID pluginUUID) {
        if (pluginDataStores.containsKey(pluginUUID))
            return;
        pluginDataStores.put(pluginUUID, new InternalPersistentDataStorage());
    }

    public void loadDataStore(UUID pluginUUID) {
        Path dataDirectory = Path.of("plugin_data");
        Path dataFile = dataDirectory.resolve(pluginUUID + ".dat");

        try {
            Files.createDirectories(dataDirectory);

            if (Files.notExists(dataFile)) {
                pluginDataStores.put(
                        pluginUUID,
                        new InternalPersistentDataStorage()
                );

                return;
            }

            byte[] serializedData = Files.readAllBytes(dataFile);

            Map<String, InternalPersistentDataStorage.Entry> data =
                    PersistentDataSerializer.deserializeData(
                            serializedData
                    );

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
        InternalPersistentDataStorage dataStore =
                pluginDataStores.get(pluginUUID);

        if (dataStore == null) {
            return;
        }

        Path directory = Path.of("plugin_data");
        Path target = directory.resolve(
                pluginUUID + ".dat"
        );
        Path temporary = directory.resolve(
                pluginUUID + ".dat.tmp"
        );

        byte[] data =
                PersistentDataSerializer.serializeData(
                        dataStore.getData()
                ).getBytes();

        while (true) {

            try {
                Files.createDirectories(directory);

                // Write the complete data first
                Files.write(
                        temporary,
                        data
                );

                // Only replace the real file after the write succeeds
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

                return;

            } catch (IOException e) {

                logger.error(
                        "CRITICAL: Failed to save plugin data for {}. " +
                                "The server will continue retrying until the data " +
                                "can be safely saved.",
                        pluginUUID,
                        e
                );
            }

            try {
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
        return pluginLoaderManager.lookupPlugin(plugin);
    }

    private void unloadPlugins() {
        pluginLoaderManager.unloadPlugins();
        commandRegistry.unregisterAll();
        scheduler.cancelAll();
        eventManager.clearEvents();
    }

    @Override
    public void reloadSoft() {
        long reloadTime = System.currentTimeMillis();
        unloadPlugins();
        pluginLoaderManager.loadPlugins();

        // Fake the bot being ready, so plugins listening for it can react
        if (discordAPI != null)
            eventManager.callEvent(new InternalBotReadyEvent(discordAPI.getAPI().getSelfUser()));
        logger.info("Reloaded (Soft) in {}s", (System.currentTimeMillis() - reloadTime) / 1000d);
    }

    @Override
    public void reloadHard() {
        long reloadTime = System.currentTimeMillis();
        unloadPlugins();

        try {
            Main.conf().load();
        } catch (IOException e) {
            logger.warn("Error during config reload: ", e);
            logger.warn("Old config will be preserved");
        }
        try {
            Main.conf().save();
        } catch (IOException e) {
            logger.warn("Error during config save: ", e);
        }

        pluginLoaderManager.loadPlugins();

        try {
            if (discordAPI != null)
                discordAPI.reconnect();

        } catch (InterruptedException e) {
            throw new RuntimeException("Reload aborted");
        }

        logger.info("Reloaded (Hard) in {}s", (System.currentTimeMillis() - reloadTime) / 1000d);
    }

    @Override
    public void registerEvents(Plugin plugin, Listener listener) {
        eventManager.registerListener(plugin, listener);
    }

    public void setDiscordApi(DiscordAPI api) {
        discordAPI = api;
    }
}

