package cloud.thehsi.ComitasBotJ.Discord;

import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageEvent;
import cloud.thehsi.ComitasBotJ.Configuration.ServerConfig;
import cloud.thehsi.ComitasBotJ.Event.EventManager;
import cloud.thehsi.ComitasBotJ.Event.Events.InternalBotConnectEvent;
import cloud.thehsi.ComitasBotJ.Event.Events.InternalMessageEvent;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscordAPI extends ListenerAdapter {
    final JDA api;
    final EventManager eventManager;
    final ServerConfig.ParsedServerConfig config;
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".DiscordAPI");

    public DiscordAPI(String BOT_TOKEN, ServerConfig.ParsedServerConfig config, EventManager eventManager) {
        api = JDABuilder
                .createDefault(BOT_TOKEN)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(this)
                .build();

        this.eventManager = eventManager;
        this.config = config;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        MessageEvent messageEvent = new InternalMessageEvent(event);

        eventManager.callEvent(messageEvent);

        if (messageEvent.isDelete()) event.getMessage().delete().queue();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);

        if (!config.botActivityName.get().isBlank())
            api.getPresence().setActivity(
                    Activity.watching(config.botActivityName.get())
            );

        eventManager.callEvent(new InternalBotConnectEvent(
                        api.getSelfUser()
        ));

        logger.info("Done ({}s)! For help, type \"help\"", Main.getRuntimeMS() / 1000d);
    }
}
