package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import cloud.thehsi.ComitasBotJ.API.Event.Events.BotReadyEvent;
import cloud.thehsi.ComitasBotJ.Bot.InternalBot;
import net.dv8tion.jda.api.entities.SelfUser;

public record InternalBotReadyEvent(SelfUser bot) implements BotReadyEvent {
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

    @Override
    public Bot getBot() {
        return new InternalBot(bot);
    }
}
