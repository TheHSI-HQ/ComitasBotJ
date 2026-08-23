package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import cloud.thehsi.ComitasBotJ.API.Event.Events.BotReadyEvent;
import cloud.thehsi.ComitasBotJ.Bot.InternalBot;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import net.dv8tion.jda.api.entities.SelfUser;
import org.jetbrains.annotations.NotNull;

public record InternalBotReadyEvent(SelfUser bot) implements BotReadyEvent {
    @Override
    public @NotNull String getUserName() {
        DebugLogging.action();
        return bot.getName();
    }

    @Override
    public @NotNull String getDisplayName() {
        DebugLogging.action();
        return bot.getEffectiveName();
    }

    @Override
    public long getId() {
        DebugLogging.action();
        return bot.getIdLong();
    }

    @Override
    public @NotNull Bot getBot() {
        DebugLogging.action();
        return new InternalBot(bot);
    }
}
