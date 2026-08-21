package cloud.thehsi.ComitasBotJ.Discord;

import ch.qos.logback.classic.Level;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Commands.InternalCommandRegistry;
import cloud.thehsi.ComitasBotJ.Event.EventManager;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class DiscordAPI {
    static JDA api;

    private static final Logger debugLogger = DebugLogging.getLogger();
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".DiscordAPI");
    private final String BOT_TOKEN;
    final DiscordAPIListeners listeners;

    public DiscordAPI(String BOT_TOKEN, EventManager eventManager, InternalCommandRegistry commandRegistry) {
        this.BOT_TOKEN = BOT_TOKEN;

        this.listeners = new DiscordAPIListeners(this, eventManager, commandRegistry);

        connect();
    }

    private DiscordAPI() {
        this.listeners = null;
        this.BOT_TOKEN = null;
    }

    public static JDA api() {
        return api;
    }

    public static DiscordAPI performMinimalStartup(String BOT_TOKEN, boolean silent) {
        ch.qos.logback.classic.Logger jdaLogger =
                (ch.qos.logback.classic.Logger)
                        LoggerFactory.getLogger("net.dv8tion.jda");

        if (DebugLogging.isAPIEnabled())
            jdaLogger.setLevel(
                    Level.DEBUG
            );
        else if (silent)
            jdaLogger.setLevel(
                    Level.WARN
            );
        else
            jdaLogger.setLevel(
                    Level.INFO
            );

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Connecting to Discord Bot in minimal mode");

        JDA api = JDABuilder.createLight(BOT_TOKEN).build();

        try {
            api.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        DiscordAPI.api = api;
        return new DiscordAPI();
    }

    private void connect() {
        ch.qos.logback.classic.Logger jdaLogger =
                (ch.qos.logback.classic.Logger)
                        LoggerFactory.getLogger("net.dv8tion.jda");

        jdaLogger.setLevel(
                DebugLogging.isAPIEnabled() ? Level.DEBUG : Level.INFO
        );

        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Connecting to Discord Bot");
        api = JDABuilder.createDefault(BOT_TOKEN)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setEventPassthrough(true)
                .addEventListeners(listeners)
                .build();
    }

    public void reconnect() throws InterruptedException {
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Reconnecting to Discord Bot");
        if (api != null) {
            if (DebugLogging.isBasicEnabled()) debugLogger.debug("Shutting bot down.");
            api.shutdown();

            if (!api.awaitShutdown(15, TimeUnit.SECONDS)) {
                if (DebugLogging.isBasicEnabled()) debugLogger.debug("Bot shutdown exceeded timeout, forcing shutdown.");
                api.shutdownNow();
            }
        }

        connect();
        if (DebugLogging.isBasicEnabled()) debugLogger.debug("Awaiting bot to be ready.");
        api.awaitReady();
    }

    public JDA getAPI() {
        return api;
    }
}
