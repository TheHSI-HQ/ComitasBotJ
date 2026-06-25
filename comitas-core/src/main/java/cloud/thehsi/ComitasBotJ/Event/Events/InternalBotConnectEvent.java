package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Event.Events.BotConnectEvent;
import net.dv8tion.jda.api.entities.SelfUser;

public class InternalBotConnectEvent implements BotConnectEvent {
    private final SelfUser bot;

    public InternalBotConnectEvent(SelfUser bot) {
        this.bot = bot;
    }

    @Override
    public String getUserName() {
        return bot.getName();
    }

    @Override
    public String getDisplayName() {
        return bot.getEffectiveName();
    }

    @Override
    public Long getId() {
        return bot.getIdLong();
    }
}
