package cloud.thehsi.ComitasBotJ.Bot;

import ch.qos.logback.classic.LoggerContext;
import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import cloud.thehsi.ComitasBotJ.API.Bot.InternalComitasImpl;
import cloud.thehsi.ComitasBotJ.API.Bot.UtilityBackend;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Scheduler;
import cloud.thehsi.ComitasBotJ.Console.ConsolePrompt;
import cloud.thehsi.ComitasBotJ.Console.TuiAppender;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Commands.InternalCommandRegistry;
import cloud.thehsi.ComitasBotJ.Discord.DiscordAPI;
import cloud.thehsi.ComitasBotJ.Event.EventManager;
import cloud.thehsi.ComitasBotJ.Main;
import cloud.thehsi.ComitasBotJ.Plugin.InternalPluginManager;
import cloud.thehsi.ComitasBotJ.Plugin.PluginLoaderManager;
import cloud.thehsi.ComitasBotJ.Scheduler.InternalScheduler;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class InternalComitas implements InternalComitasImpl {
    private static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Bot");
    private static final Logger debugLogger = DebugLogging.getLogger();
    private final ConsoleCommandRegistry consoleCommandRegistry;
    private final InternalUtilityBackend utilityBackend = new InternalUtilityBackend();
    private final ConsolePrompt consolePrompt;
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    private InternalCommandRegistry commandRegistry;
    private PluginLoaderManager pluginLoaderManager;
    private InternalPluginManager pluginManager;
    private InternalScheduler scheduler;
    private EventManager eventManager;
    private Bot bot;
    private static String bot_token;

    public InternalComitas(ConsoleCommandRegistry consoleCommandRegistry, ConsolePrompt consolePrompt) {
        this.consoleCommandRegistry = consoleCommandRegistry;
        this.consolePrompt = consolePrompt;
    }

    public static String minimalStartupAndFetchBotID() {
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Performing minimal startup to get Bot User ID");

        if (DebugLogging.isBasicEnabled()) //noinspection LoggingSimilarMessage
            debugLogger.debug("Populating Secrets");
        populateSecrets();

        if (DebugLogging.isBasicEnabled()) //noinspection LoggingSimilarMessage
            debugLogger.debug("Validating Bot Token");
        if (bot_token.isBlank()) {
            //noinspection LoggingSimilarMessage
            logger.error("Missing Discord Bot Token (./tokens.secret)");
            System.exit(1);
        }

        if (DebugLogging.isBasicEnabled())
            debugLogger.debug("Starting Bot...");
        DiscordAPI api = DiscordAPI.performMinimalStartup(bot_token, true);
        try {
            return api.getAPI().getSelfUser().getId();
        } finally {
            api.getAPI().shutdown();
        }
    }

    private static void populateSecrets() {
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
    public String getServerVersion() {
        return Main.getServerVersion();
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    @Override
    public ConsoleCommandRegistry getConsoleCommandRegistry() {
        return consoleCommandRegistry;
    }

    @Override
    public Scheduler getScheduler() {
        if (DebugLogging.isActionEnabled()) debugLogger.debug("Scheduler was requested");
        return scheduler;
    }

    @Override
    public UtilityBackend getUtilityBackend() {
        if (DebugLogging.isActionEnabled()) debugLogger.debug("UtilityBackend was requested");
        return utilityBackend;
    }

    @Override
    public Bot getBot() {
        if (DebugLogging.isActionEnabled()) debugLogger.debug("Bot was requested");
        return bot;
    }

    static final List<Runnable> onShutdownCalls = new ArrayList<>();

    public static void addShutdownCall(Runnable callback) {
        onShutdownCalls.add(callback);
    }

    @Override
    public void init() {
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Initializing Comitas API");

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Checking and creating logs directory");
        File logsDir = new File("logs");

        if (!logsDir.exists() && !logsDir.mkdir()) {
            throw new RuntimeException("Couldn't create logs folder");
        }

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Registering Shutdown Hook");
        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));

        if (DebugLogging.isBasicEnabled()) //noinspection LoggingSimilarMessage
            debugLogger.debug("Populating Secrets");
        populateSecrets();

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Validating Bot Token");
        if (bot_token.isBlank()) {
            logger.error("Missing Discord Bot Token (./tokens.secret)");
            System.exit(1);
        }

        // Prepare EventManager
        logger.info("Loading API Integrations...");
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Initializing Event Manager");
        eventManager = new EventManager();
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Initializing Scheduler");
        scheduler = new InternalScheduler();
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Initializing Command Registry");
        commandRegistry = new InternalCommandRegistry();

        // Load Plugins from ./plugins
        logger.info("Loading Plugins...");
        pluginLoaderManager = new PluginLoaderManager();

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Initializing Plugin Manager");
        pluginManager = new InternalPluginManager(
                pluginLoaderManager,
                eventManager,
                scheduler,
                commandRegistry
        );

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Loading Plugins");
        pluginLoaderManager.loadPlugins();

        logger.info("Loaded {} plugin(s).", pluginLoaderManager.count());

        // Start Bot
        logger.info("Starting Bot...");
        DiscordAPI api = new DiscordAPI(bot_token, eventManager, commandRegistry);

        pluginManager.setDiscordApi(api);

        bot = new InternalBot(api.getAPI().getSelfUser());
    }

    @Override
    public void shutdown() {
        if (DebugLogging.isActionEnabled()) debugLogger.debug("Shutdown was requested");
        onShutdown();
        System.exit(0);
    }

    private void onShutdown() {
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Marking as shutting down");
        if (!shuttingDown.compareAndSet(false, true)) return;

        Terminal terminal = consolePrompt.lineReader() != null
                ? consolePrompt.lineReader().getTerminal()
                : null;

        // Bypass printAbove()/Display entirely for shutdown logging.
        // Write straight to the terminal writer so we don't depend on
        // JLine's redraw state, which JLine's own shutdown hook may be
        // concurrently mutating.
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Setting Terminal Bypass");
        if (terminal != null) {
            TuiAppender.setBypassMode(true);
        }

        if (consolePrompt.lineReader() != null) {
            if (DebugLogging.isBasicEnabled()) debugLogger.debug("FLushing Console Output");
            consolePrompt.lineReader().getTerminal().writer().flush();
        }

        logger.info("Shutting down ComitasBotJ");

        // Unload Plugins
        if (pluginLoaderManager != null) {
            logger.info("Unloading Plugins...");
            pluginLoaderManager.unloadPlugins();
        }

        if (scheduler != null) {
            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Canceling all Schedulers");
            scheduler.cancelAll();
        }

        if (eventManager != null) {
            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Clearing all Event Listeners");
            eventManager.clearEventListeners();
        }

        if (Main.conf() != null) {
            logger.info("Writing Updated Configuration...");
            try {
                Main.conf().save();
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }

        logger.info("Bye!");

        if (terminal != null) {
            try {
                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Flushing and closing Terminal");
                terminal.writer().flush();
                terminal.close(); // deterministically close BEFORE JLine's hook can race us
            } catch (Exception ignored) {
            }
        }

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Ending Logger");
        ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();

        if (DebugLogging.isBasicEnabled()) System.out.println("Running Shutdown hooks");
        for (Runnable callback : onShutdownCalls)
            callback.run();
    }
}
