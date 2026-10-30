package cloud.thehsi.ComitasBotJ.Configuration;

import cloud.thehsi.ComitasBotJ.Main;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Secrets {
    private @NotNull
    static final Path TOKEN_PATH = DataPaths.TOKEN;
    private @NotNull
    static final String BOT_TOKEN_KEY = "bot";
    private @NotNull
    static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Secrets");

    public static boolean exists() {
        return Files.exists(TOKEN_PATH);
    }

    @NotNull
    public static String requireBotToken() {
        String token = readBotToken();
        if (token.isBlank()) {
            logger.error("Missing Discord Bot Token (./{})", TOKEN_PATH);
            System.exit(1);
        }
        return token;
    }

    @NotNull
    public static String readBotToken() {
        try {
            Properties props = load();
            String token = props.getProperty(BOT_TOKEN_KEY, "").strip();
            props.setProperty(BOT_TOKEN_KEY, token);
            store(props);
            return token;
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
            System.exit(1);
            throw new IllegalStateException(e);
        }
    }

    public static void writeBotToken(char @NotNull [] token) throws IOException {
        Properties props = load();
        props.setProperty(BOT_TOKEN_KEY, String.copyValueOf(token));
        store(props);
    }

    @NotNull
    private static Properties load() throws IOException {
        Properties props = new Properties();
        if (Files.notExists(TOKEN_PATH))
            return props;

        try (InputStream in = Files.newInputStream(TOKEN_PATH)) {
            props.load(in);
        }
        return props;
    }

    private static void store(@NotNull Properties props) throws IOException {
        Files.createDirectories(DataPaths.DATA);
        try (OutputStream out = Files.newOutputStream(TOKEN_PATH)) {
            props.store(out, "ComitasBotJ Discord Tokens");
        }
    }
}
