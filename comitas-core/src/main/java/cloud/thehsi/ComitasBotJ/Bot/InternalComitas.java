package cloud.thehsi.ComitasBotJ.Bot;

import ch.qos.logback.classic.LoggerContext;
import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import cloud.thehsi.ComitasBotJ.API.Bot.InternalComitasImpl;
import cloud.thehsi.ComitasBotJ.API.Bot.UtilityBackend;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Plugin.PluginManager;
import cloud.thehsi.ComitasBotJ.API.Scheduler.Scheduler;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Configuration.Secrets;
import cloud.thehsi.ComitasBotJ.Console.ConsolePrompt;
import cloud.thehsi.ComitasBotJ.Console.InternalConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.Console.TuiAppender;
import cloud.thehsi.ComitasBotJ.Discord.Commands.InternalCommandRegistry;
import cloud.thehsi.ComitasBotJ.Discord.DiscordAPI;
import cloud.thehsi.ComitasBotJ.Discord.InstanceLock;
import cloud.thehsi.ComitasBotJ.Event.EventManager;
import cloud.thehsi.ComitasBotJ.Main;
import cloud.thehsi.ComitasBotJ.Plugin.InternalPluginManager;
import cloud.thehsi.ComitasBotJ.Plugin.PluginLoaderManager;
import cloud.thehsi.ComitasBotJ.Sandbox.ConsolePermissionPrompter;
import cloud.thehsi.ComitasBotJ.Scheduler.InternalScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class InternalComitas implements InternalComitasImpl {
    @NotNull
    static final List<Runnable> onShutdownCalls = new ArrayList<>();
    private @NotNull
    static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Bot");
    private @NotNull
    final InternalConsoleCommandRegistry consoleCommandRegistry;
    private @NotNull
    final ConsolePrompt consolePrompt;
    private @NotNull
    final InternalUtilityBackend utilityBackend = new InternalUtilityBackend();
    private @NotNull
    final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    private @NotNull
    final EventManager eventManager = new EventManager();
    private @NotNull
    final InternalScheduler scheduler = new InternalScheduler();
    private @NotNull
    final InternalCommandRegistry commandRegistry = new InternalCommandRegistry();
    private @NotNull
    final PluginLoaderManager pluginLoaderManager = new PluginLoaderManager();
    private @NotNull
    final InternalPluginManager pluginManager;
    private @NotNull
    final InstanceLock instanceLock = new InstanceLock();
    private @Nullable InternalBot bot;

    public InternalComitas(@NotNull InternalConsoleCommandRegistry consoleCommandRegistry, @NotNull ConsolePrompt consolePrompt) {
        this.consoleCommandRegistry = consoleCommandRegistry;
        this.consolePrompt = consolePrompt;
        if (!Main.props().noCmd())
            pluginLoaderManager.security().setPrompter(new ConsolePermissionPrompter(consolePrompt));
        this.pluginManager = new InternalPluginManager(
                pluginLoaderManager,
                eventManager,
                scheduler,
                commandRegistry,
                consoleCommandRegistry
        );
    }

    @NotNull
    public static String minimalStartupAndFetchBotID() {
        DebugLogging.basic("Performing minimal startup to get Bot User ID");
        DiscordAPI.performMinimalStartup(Secrets.requireBotToken(), true);
        try {
            return DiscordAPI.api().getSelfUser().getId();
        } finally {
            DiscordAPI.api().shutdown();
        }
    }

    public static void addShutdownCall(@NotNull Runnable callback) {
        onShutdownCalls.add(callback);
    }

    private static void info(@NotNull String message, @Nullable Object... args) {
        if (!Main.props().quiet()) logger.info(message, args);
    }

    @Override
    public void init() {
        DebugLogging.basic("Initializing Comitas API");
        createLogsDirectory();
        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));

        String token = Secrets.requireBotToken();
        loadPlugins();
        connect(token);
    }

    private void createLogsDirectory() {
        DebugLogging.basic("Checking and creating logs directory");
        try {
            Files.createDirectories(Path.of("logs"));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create logs folder", e);
        }
    }

    private void loadPlugins() {
        info("Loading Plugins...");
        pluginLoaderManager.loadPlugins();
        info("Loaded {} plugin(s).", pluginLoaderManager.count());
    }

    private void connect(@NotNull String token) {
        info("Starting Bot...");
        pluginManager.setDiscordApi(new DiscordAPI(token, eventManager, commandRegistry, pluginManager, instanceLock));
    }

    @Override
    public void shutdown() {
        DebugLogging.action();
        onShutdown();
        System.exit(0);
    }

    private void onShutdown() {
        if (!shuttingDown.compareAndSet(false, true)) return;

        Terminal terminal = consolePrompt.lineReader().getTerminal();
        enterTerminalBypass(terminal);

        logger.info("Shutting down ComitasBotJ");
        stopServices();
        logger.info("Bye!");

        closeTerminal(terminal);

        DebugLogging.basic("Ending Logger");
        ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();

        if (DebugLogging.isBasicEnabled()) System.out.println("Running Shutdown hooks");
        onShutdownCalls.forEach(Runnable::run);
    }

    private void enterTerminalBypass(@Nullable Terminal terminal) {
        if (terminal == null) return;

        // Bypass printAbove()/Display entirely for shutdown logging, as JLine's own
        // shutdown hook may be concurrently mutating its redraw state.
        DebugLogging.basic("Setting Terminal Bypass");
        TuiAppender.setBypassMode(true);

        try {
            terminal.writer().flush();
        } catch (IllegalStateException e) {
            DebugLogging.basic("Cannot flush console output. Continuing");
        }
    }

    private void stopServices() {
        logger.info("Unloading Plugins...");
        pluginLoaderManager.unloadPlugins();

        DebugLogging.basic("Canceling all Schedulers");
        scheduler.cancelAll();

        DebugLogging.basic("Shutting down Event Listeners");
        eventManager.shutdown();

        instanceLock.release();

        DebugLogging.basic("Disconnecting from Discord");
        DiscordAPI.disconnect();
    }

    private void closeTerminal(@Nullable Terminal terminal) {
        if (terminal == null) return;

        DebugLogging.basic("Flushing and closing Terminal");
        try {
            terminal.writer().flush();
            terminal.close();
        } catch (Exception ignored) {
        }
    }

    @Override
    public @NotNull String getServerVersion() {
        DebugLogging.action();
        return Main.getServerVersion();
    }

    @Override
    public @NotNull PluginManager getPluginManager() {
        DebugLogging.action();
        return pluginManager;
    }

    @Override
    public @NotNull CommandRegistry getCommandRegistry() {
        DebugLogging.action();
        return commandRegistry;
    }

    @Override
    public @NotNull ConsoleCommandRegistry getConsoleCommandRegistry() {
        DebugLogging.action();
        return consoleCommandRegistry;
    }

    @Override
    public @NotNull Scheduler getScheduler() {
        DebugLogging.action();
        return scheduler;
    }

    @Override
    public @NotNull UtilityBackend getUtilityBackend() {
        DebugLogging.action();
        return utilityBackend;
    }

    @Override
    public @NotNull Bot getBot() {
        DebugLogging.action();
        var api = DiscordAPI.nullableApi();
        if (api != null && (bot == null || bot.bot().getJDA() != api))
            bot = new InternalBot(api.getSelfUser(), eventManager);
        if (bot == null)
            throw new IllegalStateException("Bot not initialized");

        return bot;
    }
}
