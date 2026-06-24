package cloud.thehsi.ComitasBotJ;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.Bot.InternalComitas;
import cloud.thehsi.ComitasBotJ.Console.ConsolePrompt;
import cloud.thehsi.ComitasBotJ.Console.InternalConsoleCommandRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
    private static final long STARTUP_TIME = System.currentTimeMillis();

    private static final ConsoleCommandRegistry consoleCommandRegistry = new ConsoleCommandRegistry(new InternalConsoleCommandRegistry());
    private static final ConsolePrompt consolePrompt = new ConsolePrompt(consoleCommandRegistry);
    private static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH);

    public static final String LOGGER_ROOT_PATH = "ComitasBotJ";

    public static long getRuntimeMS() {
        return System.currentTimeMillis() - STARTUP_TIME;
    }

    public static void main(String[] args) {
        System.out.println("""
   ___           _ _           ___      _      _\s
  / __|___ _ __ (_) |_ __ _ __| _ ) ___| |_ _ | |
 | (__/ _ \\ '  \\| |  _/ _` (_-< _ \\/ _ \\  _| || |
  \\___\\___/_|_|_|_|\\__\\__,_/__/___/\\___/\\__|\\__/\s
 """);


        logger.info("Starting ComitasBotJ v{}...", getVersion());

        Comitas comitas = Comitas.getInstance();
        comitas.init(new InternalComitas(consoleCommandRegistry));

        consolePrompt.run();

        /*ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

        exec.scheduleAtFixedRate(() -> {
            logger.info("This is a Test");
            logger.warn("This is a Test");
            logger.error("This is a Test");
        }, 0, 1500, TimeUnit.MILLISECONDS);*/

        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {
        }
    }

    public static String getVersion() {
        try (InputStream in = Main.class.getResourceAsStream("/version.properties")) {
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}