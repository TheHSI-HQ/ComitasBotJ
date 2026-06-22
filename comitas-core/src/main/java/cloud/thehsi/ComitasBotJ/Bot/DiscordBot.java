package cloud.thehsi.ComitasBotJ.Bot;

import cloud.thehsi.ComitasBotJ.API.Event.EventManager;
import cloud.thehsi.ComitasBotJ.API.Event.Events.BotConnectEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageEvent;
import cloud.thehsi.ComitasBotJ.Configuration.ServerConfig;
import cloud.thehsi.ComitasBotJ.Event.Events.InternalMessageEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

public class DiscordBot extends ListenerAdapter {
    JDA api;
    EventManager eventManager;
    ServerConfig.ParsedServerConfig config;

    public DiscordBot(String BOT_TOKEN, ServerConfig.ParsedServerConfig config, EventManager eventManager) {
        api = JDABuilder
                .createDefault(BOT_TOKEN)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(this)
                .build();

        this.eventManager = eventManager;
        this.config = config;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        MessageEvent messageEvent = new MessageEvent(new InternalMessageEvent(event));

        eventManager.callEvent(messageEvent);

        if (messageEvent.isDelete()) event.getMessage().delete().queue();
//
//        if (event.getAuthor().isBot()) return;
//        // We don't want to respond to other bot accounts, including ourself
//        Message message = event.getMessage();
//        String content = message.getContentRaw();
//        // getContentRaw() is an atomic getter
//        // getContentDisplay() is a lazy getter which modifies the content for e.g. console view (strip discord formatting)
//        if (content.equals("!ping")) {
//            MessageChannel channel = event.getChannel();
//            channel.sendMessage("Pong!").queue(); // Important to call .queue() on the RestAction returned by sendMessage(...)
//        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);

        if (!config.botActivityName.get().isBlank())
            api.getPresence().setActivity(
                    Activity.watching(config.botActivityName.get())
            );


        eventManager.callEvent(new BotConnectEvent(api.getSelfUser().getName()));
    }


}
