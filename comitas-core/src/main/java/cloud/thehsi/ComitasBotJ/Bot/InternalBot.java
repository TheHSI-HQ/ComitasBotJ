package cloud.thehsi.ComitasBotJ.Bot;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import cloud.thehsi.ComitasBotJ.API.Bot.InternalBotImpl;
import cloud.thehsi.ComitasBotJ.API.Event.EventManager;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
import cloud.thehsi.ComitasBotJ.API.PluginLoader.PluginLoaderManager;
import cloud.thehsi.ComitasBotJ.Configuration.ServerConfig;
import cloud.thehsi.ComitasBotJ.Event.InternalEventManager;
import cloud.thehsi.ComitasBotJ.Plugin.InternalPluginManager;
import cloud.thehsi.ComitasBotJ.PluginLoader.InternalPluginLoaderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class InternalBot implements InternalBotImpl {
    private PluginLoaderManager pluginLoaderManager;
    private PluginManager pluginManager;
    private ServerConfig.ParsedServerConfig serverConfig;
    private EventManager eventManager;
    private Logger logger;

    private DiscordBot discordBot;

    private String bot_token;

    private void populateSecrets() {
        Path token_path = Path.of("tokens.secret");
        File token_file = new File(token_path.toUri());

        try {
            if (!token_file.exists() && !token_file.createNewFile())
                throw new IOException("Unknown error when creating token.secret");
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
            System.exit(1);
        }

        Properties token_props = new Properties();

        try (InputStream in = Files.newInputStream(token_path)) {
            token_props.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        bot_token = token_props.getProperty("bot", "").strip();
        token_props.setProperty("bot", bot_token);

        try (OutputStream out = Files.newOutputStream(token_path)) {
            token_props.store(out, "ComitasBotJ Discord Tokens");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        logger = LoggerFactory.getLogger(Bot.class);

        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));

        // Load Configuration from ./server.properties
        logger.info("Loading Configuration...");
        try {
            ServerConfig rawServerConfig = new ServerConfig();

            serverConfig = rawServerConfig.asParsed();

            serverConfig.save();
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
            System.exit(1);
        }

        populateSecrets();

        if (bot_token.isBlank()) {
            logger.error("Missing Discord Bot Token (./tokens.secret)");
            System.exit(1);
        }

        logger.info("Loaded {} configuration value(s).", serverConfig.count());

        if (!serverConfig.enabled.get()) {
            logger.warn("This Server is disabled!");
            logger.warn("To change this, go to ./server.properties and set enabled=true");
            logger.warn("This Server will now shut down");
            System.exit(0);
        }

        // Prepare EventManager
        logger.info("Loading EventManager...");
        eventManager = new EventManager(new InternalEventManager());

        // Load Plugins from ./plugins
        logger.info("Loading Plugins...");
        pluginLoaderManager = new PluginLoaderManager(new InternalPluginLoaderManager());

        pluginManager = new PluginManager(new InternalPluginManager(
                pluginLoaderManager,
                eventManager
        ));

        pluginLoaderManager.loadPlugins();

        logger.info("Loaded {} plugin(s).", pluginLoaderManager.count());

        // Start Bot
        logger.info("Starting Bot...");
        discordBot = new DiscordBot(bot_token, serverConfig, eventManager);
    }

    private void onShutdown() {
        logger.info("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        logger.info("Shutting down ComitasBotJ");

        // Unload Plugins
        if (pluginLoaderManager != null) {
            logger.info("Unloading Plugins...");
            pluginLoaderManager.unloadPlugins();
        }

        if (serverConfig != null) {
            logger.info("Writing Updated Configuration...");
            try {
                serverConfig.save();
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }

        logger.info("Bye!");
    }
}
