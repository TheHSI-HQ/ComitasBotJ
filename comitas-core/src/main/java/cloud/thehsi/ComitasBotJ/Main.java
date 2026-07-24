package cloud.thehsi.ComitasBotJ;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.Bot.InternalComitas;
import cloud.thehsi.ComitasBotJ.Console.ConsolePrompt;
import cloud.thehsi.ComitasBotJ.Console.InternalConsoleCommandRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@CommandLine.Command(
        name = "comitas",
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider.class,
        description = "ComitasBotJ"
)
public class Main implements Runnable {
    private static final long STARTUP_TIME = System.currentTimeMillis();

    private static final ConsoleCommandRegistry consoleCommandRegistry = new InternalConsoleCommandRegistry();
    private static final ConsolePrompt consolePrompt = new ConsolePrompt(consoleCommandRegistry);
    private static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH);

    public static final String LOGGER_ROOT_PATH = "ComitasBotJ";

    public static long getRuntimeMS() {
        return System.currentTimeMillis() - STARTUP_TIME;
    }

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

    // Properties
    private static StartupProperties props;
    public static StartupProperties props() {
        return props;
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

    @Override
    public void run() {
        logger.info("Starting ComitasBotJ v{}...", getServerVersion());

        Main.props = new StartupProperties(
                noCmd, ignoreApiTarget
        );

        if (props.ignoreApiTarget())
            logger.warn("""
                    {}
                    Plugin compatibility checks are disabled.
                    
                    ComitasBotJ will load plugins regardless of their compatibility
                    status. This feature is intended for development and testing only
                    and must not be used in production environments.
                    
                    Proceed with caution.
                    """, ConsoleColor.YELLOW);

        Comitas comitas = Comitas.getInstance();
        comitas.init(new InternalComitas(props, consoleCommandRegistry));

        if (!noCmd)
            consolePrompt.run();

        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {
        }
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

            public static String getAPIVersion() {
                try (InputStream in = Main.class.getResourceAsStream("/version.properties")) {
                    Properties props = new Properties();
                    props.load(in);
                    return props.getProperty("api-version");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }