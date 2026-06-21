package cloud.thehsi.ComitasBotJ;

import cloud.thehsi.ComitasBotJ.Bot.Bot;
import cloud.thehsi.ComitasBotJ.Bot.InternalBot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger =
            LogManager.getLogger(Main.class);

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

        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {
        }
    }
}