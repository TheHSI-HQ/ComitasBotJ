package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import cloud.thehsi.ComitasBotJ.API.Event.Events.BotReadyEvent;
import cloud.thehsi.ComitasBotJ.Bot.InternalBot;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import net.dv8tion.jda.api.entities.SelfUser;

public record InternalBotReadyEvent(SelfUser bot) implements BotReadyEvent {
    @Override
    public String getUserName() {
        DebugLogging.action();
        return bot.getName();
    }

    @Override
    public String getDisplayName() {
        DebugLogging.action();
        return bot.getEffectiveName();
    }

    @Override
    public Long getId() {
        DebugLogging.action();
        return bot.getIdLong();
    }

    @Override
    public Bot getBot() {
        DebugLogging.action();
        return new InternalBot(bot);
    }
}
