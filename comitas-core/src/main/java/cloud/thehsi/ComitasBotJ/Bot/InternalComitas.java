package cloud.thehsi.ComitasBotJ.Bot;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Bot.InternalComitasImpl;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
import cloud.thehsi.ComitasBotJ.Configuration.ServerConfig;
import cloud.thehsi.ComitasBotJ.Discord.DiscordAPI;
import cloud.thehsi.ComitasBotJ.Event.EventManager;
import cloud.thehsi.ComitasBotJ.Main;
import cloud.thehsi.ComitasBotJ.Plugin.InternalPluginManager;
import cloud.thehsi.ComitasBotJ.Plugin.PluginLoaderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class InternalComitas implements InternalComitasImpl {
    private PluginLoaderManager pluginLoaderManager;
    private PluginManager pluginManager;
    private ServerConfig.ParsedServerConfig serverConfig;
    private final ConsoleCommandRegistry consoleCommandRegistry;
    private Logger logger;

    private String bot_token;

    public InternalComitas(ConsoleCommandRegistry consoleCommandRegistry) {
        this.consoleCommandRegistry = consoleCommandRegistry;
    }

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
    public String getAPIVersion() {
        try (InputStream in = Comitas.class.getResourceAsStream("/version.properties")) {
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getServerVersion() {
        return Main.getVersion();
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public ConsoleCommandRegistry getConsoleCommandRegistry() {
        return consoleCommandRegistry;
    }

    @Override
    public void init(InternalComitasImpl impl) {
        File logsDir = new File("logs");

        if (!logsDir.exists() && !logsDir.mkdir()) {
            throw new RuntimeException("Couldn't create logs folder");
        }

        logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Bot");

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
        EventManager eventManager = new EventManager();

        // Load Plugins from ./plugins
        logger.info("Loading Plugins...");
        pluginLoaderManager = new PluginLoaderManager();

        pluginManager = new PluginManager(new InternalPluginManager(
                pluginLoaderManager,
                eventManager
        ));

        pluginLoaderManager.loadPlugins();

        logger.info("Loaded {} plugin(s).", pluginLoaderManager.count());

        // Start Bot
        logger.info("Starting Bot...");
        new DiscordAPI(bot_token, serverConfig, eventManager);
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
