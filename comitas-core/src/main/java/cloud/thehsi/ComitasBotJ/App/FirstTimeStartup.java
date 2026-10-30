package cloud.thehsi.ComitasBotJ.App;

import cloud.thehsi.ComitasBotJ.Configuration.Secrets;
import cloud.thehsi.ComitasBotJ.Main;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;
import java.io.IOException;

public class FirstTimeStartup {
    private @NotNull
    static final Logger log = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".FirstTimeStartup");

    public static boolean shouldFirstTimeStartup() {
        return !Secrets.exists();
    }

    public static void showFirstTimeStartup() {
        log.info("""
                
                ========================================
                         Welcome to ComitasBotJ
                ========================================
                
                This appears to be your first time
                starting ComitasBotJ.
                
                You will now be guided through the
                initial setup process.
                
                To skip this setup, restart ComitasBotJ
                with the --skip-fts argument, or press
                Enter at the next prompt.
                
                ----------------------------------------
                Discord Bot Authentication
                ----------------------------------------
                
                A Discord bot token is required for
                ComitasBotJ to function correctly.
                
                Your token will be hidden while entered.
                
                """);

        Console console = System.console();

        if (console == null) {
            log.error(
                    "No system console available. " +
                            "Please run ComitasBotJ from a terminal."
            );
            return;
        }

        char[] token = console.readPassword(
                "Enter your Discord bot token: "
        );

        if (token == null || token.length == 0) {
            log.info("No token provided. Skipping initial token setup.");
            return;
        }

        try {
            Secrets.writeBotToken(token);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            java.util.Arrays.fill(token, '\0');
        }
    }
}
