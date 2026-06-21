package cloud.thehsi.ComitasBotJ;

import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
import cloud.thehsi.ComitasBotJ.Configuration.ServerConfig;
import cloud.thehsi.ComitasBotJ.PluginLoader.PluginLoaderManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Bot {
    private static final Bot INSTANCE = new Bot();

    private static Logger logger;

    private final PluginLoaderManager pluginLoaderManager;
    private final PluginManager pluginManager;
    private ServerConfig.ParsedServerConfig serverConfig;

    private Bot() {
        // Load Configuration from ./server.properties
        logger = LogManager.getLogger(Bot.class);

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
        pluginLoaderManager = new PluginLoaderManager();
        pluginLoaderManager.loadPlugins();

        pluginManager = new PluginManager(pluginLoaderManager);
        logger.info("Loaded {} plugin(s).", pluginLoaderManager.count());

        pluginLoaderManager.loadPlugins();
    }

    public static Bot getInstance() {
        return INSTANCE;
    }

    public static PluginManager getPluginManager() {
        return INSTANCE.pluginManager;
    }
}
