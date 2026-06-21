package cloud.thehsi.ComitasBotJ.Bot;

import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
import cloud.thehsi.ComitasBotJ.Configuration.ServerConfig;
import cloud.thehsi.ComitasBotJ.PluginLoader.InternalPluginLoaderManager;
import cloud.thehsi.ComitasBotJ.PluginLoader.PluginLoaderManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class InternalBot implements InternalBotImpl {
    private PluginLoaderManager pluginLoaderManager;
    private PluginManager pluginManager;
    private ServerConfig.ParsedServerConfig serverConfig;
    private Logger logger;

    @Override
    public String getDiscordToken() {
        return "123";
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public void init(InternalBotImpl impl) {
        File logsDir = new File("logs");

        if (!logsDir.exists() && !logsDir.mkdir()) {
            throw new RuntimeException("Couldn't create logs folder");
        }

        logger = LogManager.getLogger(Bot.class);

        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));

        // Load Configuration from ./server.properties
        logger.info("Loading Configuration...");
        try {
            ServerConfig rawServerConfig = new ServerConfig();

            serverConfig = rawServerConfig.asParsed();

            serverConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        logger.info("Loaded {} configuration value(s).", serverConfig.count());

        if (!serverConfig.enabled.get()) {
            logger.warn("This Server is disabled!");
            logger.warn("To change this, go to ./server.properties and set enabled=true");
            logger.warn("This Server will now shut down");
            System.exit(0);
        }

        // Load Plugins from ./plugins
        logger.info("Loading Plugins...");
        pluginLoaderManager = new PluginLoaderManager(new InternalPluginLoaderManager());
        pluginLoaderManager.loadPlugins();

        pluginManager = new PluginManager(pluginLoaderManager);
        logger.info("Loaded {} plugin(s).", pluginLoaderManager.count());
    }

    private void onShutdown() {
        logger.info("Shutting down ComitasBotJ");

        // Unload Plugins
        logger.info("Unloading Plugins...");
        pluginLoaderManager.unloadPlugins();

        logger.info("Writing Updated Configuration...");
        try {
            serverConfig.save();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }

        logger.info("Bye!");
    }
}
