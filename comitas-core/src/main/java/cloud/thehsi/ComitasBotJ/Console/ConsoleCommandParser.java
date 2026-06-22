package cloud.thehsi.ComitasBotJ.Console;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConsoleCommandParser {
    private static final Logger logger = LoggerFactory.getLogger("Console");

    public static void parseCommand(String command) {
        switch (command.strip()) {
            case "plugins", "pl" ->
                    logger.info("Loaded Plugins: {}", String.join(", ", Bot.getPluginManager().getPluginNames()));
            case "stop", "exit", "quit", "die" -> System.exit(0);
            case "reload", "rl" -> Bot.getPluginManager().reloadPlugins();
            case "" -> {
            }

            default -> logger.error("Unknown command: {}", command);
        }
    }
}
