package cloud.thehsi.ComitasBotJ.App;

import cloud.thehsi.ComitasBotJ.Main;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class FirstTimeStartup {
    private @NotNull
    static final Logger log = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".FirstTimeStartup");

    public static boolean shouldFirstTimeStartup() {
        Path token_path = Path.of("tokens.secret");
        File token_file = new File(token_path.toUri());

        return !token_file.exists();
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
            Path token_path = Path.of("tokens.secret");
            File token_file = new File(token_path.toUri());

            if (!token_file.exists() && !token_file.createNewFile())
                throw new RuntimeException("Unknown error when creating token.secret");

            Properties token_props = new Properties();
            try (InputStream in = Files.newInputStream(token_path)) {
                token_props.load(in);
            }

            token_props.setProperty("bot", String.copyValueOf(token));

            try (OutputStream out = Files.newOutputStream(token_path)) {
                token_props.store(out, "ComitasBotJ Discord Tokens");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            java.util.Arrays.fill(token, '\0');
        }
    }
}
