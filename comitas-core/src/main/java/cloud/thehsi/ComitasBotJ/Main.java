package cloud.thehsi.ComitasBotJ;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.App.FirstTimeStartup;
import cloud.thehsi.ComitasBotJ.App.StartupWarnings;
import cloud.thehsi.ComitasBotJ.App.VersionProvider;
import cloud.thehsi.ComitasBotJ.Bot.InternalComitas;
import cloud.thehsi.ComitasBotJ.Configuration.ServerConfig;
import cloud.thehsi.ComitasBotJ.Configuration.StartupProperties;
import cloud.thehsi.ComitasBotJ.Console.ConsolePrompt;
import cloud.thehsi.ComitasBotJ.Console.InternalConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.Plugin.PluginLister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

@CommandLine.Command(
        name = "comitas",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        description = "ComitasBotJ"
)
public class Main implements Runnable {
    public @NotNull
    static final String LOGGER_ROOT_PATH = "ComitasBotJ";
    private static final long STARTUP_TIME = System.currentTimeMillis();
    private @NotNull
    static final InternalConsoleCommandRegistry consoleCommandRegistry = new InternalConsoleCommandRegistry();
    private @NotNull
    static final ConsolePrompt consolePrompt = new ConsolePrompt(consoleCommandRegistry);
    private @NotNull
    static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH);
    private @Nullable
    static StartupProperties props;
    private @Nullable
    static ServerConfig.ParsedServerConfig conf;

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
            names = {"-q", "--quiet"},
            description = "Quiet mode, supresses all info logging."
    )
    private boolean quiet;

    @CommandLine.Option(
            names = { "--list-plugins", "-l" },
            description = "List all plugins, regardless of whitelists, and exits."
    )
    private boolean listPlugins;

    @CommandLine.Option(
            names = {"--skip-fts"},
            description = "Skip the first time startup screen."
    )
    private boolean skipFirstTimeStartup;

    @CommandLine.Option(
            names = { "--invite", "-i" },
            description = "Generates an invite for the bot and exits.",
            arity = "0..1",
            fallbackValue = "8",
            defaultValue = "0"
    )
    private long invitePermissions;

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

    @NotNull
    public static StartupProperties props() {
        if (props == null)
            throw new IllegalStateException("Properties not initialized");
        return props;
    }

    @NotNull
    public static ServerConfig.ParsedServerConfig conf() {
        if (conf == null)
            throw new IllegalStateException("ServerConfig not initialized");
        return conf;
    }

    public static void main(@NotNull String[] args) {
        //noinspection JavaPrintToLogpoint
        System.out.println("" + ConsoleColor.BRIGHT_WHITE + ConsoleColor.BOLD + """
                  ___           _ _           ___      _      _\s
                 / __|___ _ __ (_) |_ __ _ __| _ ) ___| |_ _ | |
                | (__/ _ \\ '  \\| |  _/ _` (_-< _ \\/ _ \\  _| || |
                 \\___\\___/_|_|_|_|\\__\\__,_/__/___/\\___/\\__|\\__/\s
                """);

        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    private @NotNull
    static final String SERVER_VERSION = loadServerVersion();

    @NotNull
    private static String loadServerVersion() {
        try (InputStream in = Main.class.getResourceAsStream("/version.properties")) {
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static String getServerVersion() {
        return SERVER_VERSION;
    }

    private static void info(@NotNull String message, @Nullable Object... args) {
        if (!props().quiet()) logger.info(message, args);
    }

    @Override
    public void run() {
        props = createStartupProperties();
        info("Starting ComitasBotJ v{}...", getServerVersion());

        if (!skipFirstTimeStartup && FirstTimeStartup.shouldFirstTimeStartup())
            FirstTimeStartup.showFirstTimeStartup();

        conf = loadConfig();

        if (props.invitePermissions() != 0) printInviteAndExit();
        if (props.listPlugins()) listPluginsAndExit();
        if (!conf.enabled.get()) disabledExit();

        StartupWarnings.log(props, conf);
        startComitas();
        awaitShutdown();
    }

    @NotNull
    private StartupProperties createStartupProperties() {
        DebugLogging.basic("Creating Startup Properties");
        return new StartupProperties(
                noCmd, ignoreApiTarget, safeMode,
                strictSafeMode, quiet && !DebugLogging.isBasicEnabled(),
                listPlugins, invitePermissions
        );
    }

    @NotNull
    private ServerConfig.ParsedServerConfig loadConfig() {
        info("Loading Configuration...");
        try {
            DebugLogging.basic("Reading and Rewriting ServerConfig");
            ServerConfig.ParsedServerConfig config = new ServerConfig().asParsed();
            config.save();
            info("Loaded {} configuration value(s).", config.count());
            return config;
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
            System.exit(1);
            throw new IllegalStateException(e);
        }
    }

    private void startComitas() {
        DebugLogging.basic("Initializing Comitas API");
        Comitas.getInstance().init(new InternalComitas(consoleCommandRegistry, consolePrompt));

        if (!props().noCmd())
            consolePrompt.run();
    }

    private void awaitShutdown() {
        DebugLogging.basic("Sleeping Main Process");
        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {
        }
    }

    private void printInviteAndExit() {
        info("Generating invite...");

        if (props().invitePermissions() == 8)
            logger.warn("No permissions specified (or Administrator permission specified), granting Administator permission (8). Not recommended for production");

        Permission[] permissions = Permission.fromLong(props().invitePermissions());
        info("Granting permissions: {}",
                String.join(", ", Arrays.stream(permissions).map(Enum::name).toArray(String[]::new))
        );

        logger.info(
                "Invitation Link: {}{}https://discord.com/oauth2/authorize?client_id={}&scope=bot&permissions={}{}",
                ConsoleColor.BRIGHT_BLUE,
                ConsoleColor.BOLD,
                InternalComitas.minimalStartupAndFetchBotID(),
                Permission.asLong(permissions),
                ConsoleColor.RESET
        );

        exit();
    }

    private void listPluginsAndExit() {
        info("Loadable Plugins:");
        PluginLister.listAllPlugins();
        exit();
    }

    private void disabledExit() {
        logger.warn("This Server is disabled!");
        logger.warn("To change this, go to ./server.properties and set enabled=true");
        logger.warn("This Server will now shut down");
        System.exit(0);
    }

    private void exit() {
        info("Exiting...");
        System.exit(0);
    }
}
