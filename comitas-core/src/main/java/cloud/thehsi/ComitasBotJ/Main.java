package cloud.thehsi.ComitasBotJ;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.Bot.InternalComitas;
import cloud.thehsi.ComitasBotJ.Configuration.ServerConfig;
import cloud.thehsi.ComitasBotJ.Configuration.StartupProperties;
import cloud.thehsi.ComitasBotJ.Console.ConsolePrompt;
import cloud.thehsi.ComitasBotJ.Console.InternalConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.Plugin.PluginLister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

@CommandLine.Command(
        name = "comitas",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        description = "ComitasBotJ"
)
public class Main implements Runnable {
    public static final String LOGGER_ROOT_PATH = "ComitasBotJ";
    private static final long STARTUP_TIME = System.currentTimeMillis();
    private static final InternalConsoleCommandRegistry consoleCommandRegistry = new InternalConsoleCommandRegistry();
    private static final ConsolePrompt consolePrompt = new ConsolePrompt(consoleCommandRegistry);
    private static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH);
    private final Logger debugLogger = DebugLogging.getLogger();

    // Properties
    private static StartupProperties props;
    private static ServerConfig.ParsedServerConfig conf;
    @CommandLine.Option(
            names = "--no-cmd",
            description = "Disable command line"
    )
    private boolean noCmd;
    @CommandLine.Option(
            names = "--ignore-api-target",
            description = "Ignore API Version specification in Plugins"
    )
    private boolean ignoreApiTarget;
    @CommandLine.Option(
            names = { "--safe-mode", "-s" },
            description = "Enable safe mode (No Plugin loading)."
    )
    private boolean safeMode;
    @CommandLine.Option(
            names = "--strict-safe-mode",
            description = "Same as safe mode but also skips base plugin."
    )
    private boolean strictSafeMode;
    @CommandLine.Option(
            names = { "--list-plugins", "-l" },
            description = "List all plugins, regardless of whitelists, and exits."
    )
    private boolean listPlugins;

    @CommandLine.Option(
            names = { "--invite", "-i" },
            description = "Generates an invite for the bot and exits."
    )
    private boolean generateInvite;

    @CommandLine.Option(
            names = { "--debug", "--verbose" },
            description = "Enables verbose logging. Optionally specify a debug level.",
            arity = "0..1",
            fallbackValue = "1",
            defaultValue = "0"
    )
    public static int debugLevel;

    public static long getRuntimeMS() {
        return System.currentTimeMillis() - STARTUP_TIME;
    }

    public static StartupProperties props() {
        return props;
    }

    public static ServerConfig.ParsedServerConfig conf() {
        return conf;
    }

    public static void main(String[] args) {
        System.out.println("" + ConsoleColor.BRIGHT_WHITE + ConsoleColor.BOLD + """
                  ___           _ _           ___      _      _\s
                 / __|___ _ __ (_) |_ __ _ __| _ ) ___| |_ _ | |
                | (__/ _ \\ '  \\| |  _/ _` (_-< _ \\/ _ \\  _| || |
                 \\___\\___/_|_|_|_|\\__\\__,_/__/___/\\___/\\__|\\__/\s
                """);

        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    public static String getServerVersion() {
        try (InputStream in = Main.class.getResourceAsStream("/version.properties")) {
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        logger.info("Starting ComitasBotJ v{}...", getServerVersion());

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Creating Startup Properties");
        Main.props = new StartupProperties(
                noCmd, ignoreApiTarget, safeMode,
                strictSafeMode, listPlugins, generateInvite
        );

        // Load Configuration from ./server.properties
        logger.info("Loading Configuration...");
        try {
            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Creating and Reading ServerConfig");
            ServerConfig rawServerConfig = new ServerConfig();
            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Parsing ServerConfig");
            conf = rawServerConfig.asParsed();
            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Rewriting ServerConfig");
            conf.load();
            conf.save();
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
            System.exit(1);
        }

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Computing config values");
        takeConfigActions();

        logger.info("Loaded {} configuration value(s).", conf.count());

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Initializing Comitas API");
        Comitas comitas = Comitas.getInstance();
        comitas.init(new InternalComitas(consoleCommandRegistry, consolePrompt));

        if (!noCmd)
            consolePrompt.run();

        try {
            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Sleeping Main Process");
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {
        }
    }

    private void takeConfigActions() {
        if (props().generateInvite()) {
            logger.info("Generating invite...");
            logger.info(
                    "Invitation Link: {}{}https://discord.com/oauth2/authorize?client_id={}&scope=bot&permissions=8{}",
                    ConsoleColor.BRIGHT_BLUE,
                    ConsoleColor.BOLD,
                    InternalComitas.minimalStartupAndFetchBotID(),
                    ConsoleColor.RESET
            );
            logger.info("Exiting...");
            System.exit(0);
        }

        if (props().listPlugins()) {
            logger.info("Loadable Plugins:");
            PluginLister.listAllPlugins();
            logger.info("Exiting...");
            System.exit(0);
        }

        if (!conf().enabled.get()) {
            logger.warn("This Server is disabled!");
            logger.warn("To change this, go to ./server.properties and set enabled=true");
            logger.warn("This Server will now shut down");
            System.exit(0);
        }

        if (Objects.equals(conf().allowedPlugins.get(), "*")) {
            logger.warn("""
                        {}
                        Plugin whitelist is not enabled.
                
                        For production environments, enabling the plugin whitelist is
                        strongly recommended to prevent unexpected or untrusted plugins
                        from being loaded.
                        """, ConsoleColor.YELLOW);
        }

        if (props.ignoreApiTarget())
            logger.warn("""
                        {}
                        Plugin compatibility checks are disabled.
                        
                        ComitasBotJ will load plugins regardless of their compatibility
                        status. This feature is intended for development and testing only
                        and must not be used in production environments.
                        
                        Proceed with caution.
                        """, ConsoleColor.YELLOW);

        if (props.safeMode() && !props().strictSafeMode())
            logger.warn("""
                    {}
                    Safe Mode is enabled.
                    
                    ComitasBotJ will not load any plugins.
                    This mode is intended only for debugging and testing
                    and should not be used in production environments.
                    """, ConsoleColor.YELLOW);

        if (props.strictSafeMode())
            logger.warn("""
                    {}
                    Strict Safe Mode is enabled.
                    
                    Plugin loading and all built-in commands are disabled.
                    To quit ComitasBotJ, press Ctrl+C (^C).
                    
                    This mode is intended only for debugging and testing
                    and should not be used in production environments.
                    """, ConsoleColor.YELLOW);
    }
}