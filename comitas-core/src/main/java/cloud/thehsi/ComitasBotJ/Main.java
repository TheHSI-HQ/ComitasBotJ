package cloud.thehsi.ComitasBotJ;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import cloud.thehsi.ComitasBotJ.Bot.InternalBot;
import cloud.thehsi.ComitasBotJ.Console.ConsolePrompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final ConsolePrompt consolePrompt = new ConsolePrompt();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("""
   ___           _ _           ___      _      _\s
  / __|___ _ __ (_) |_ __ _ __| _ ) ___| |_ _ | |
 | (__/ _ \\ '  \\| |  _/ _` (_-< _ \\/ _ \\  _| || |
  \\___\\___/_|_|_|_|\\__\\__,_/__/___/\\___/\\__|\\__/\s
 """);


        logger.info("Starting ComitasBotJ...");

        Bot bot = Bot.getInstance();
        bot.init(new InternalBot());

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
}