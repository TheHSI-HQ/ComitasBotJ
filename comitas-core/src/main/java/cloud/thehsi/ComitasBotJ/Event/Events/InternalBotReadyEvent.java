package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import cloud.thehsi.ComitasBotJ.API.Event.Events.BotReadyEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import org.jetbrains.annotations.NotNull;

public record InternalBotReadyEvent(Bot bot) implements BotReadyEvent {
    @Override
    public @NotNull String getUserName() {
        DebugLogging.action();
        return bot.getUserName();
    }

    @Override
    public @NotNull String getDisplayName() {
        DebugLogging.action();
        return bot.getDisplayName();
    }

    @Override
    public long getId() {
        DebugLogging.action();
        return bot.getId();
    }

    @Override
    public @NotNull Bot getBot() {
        DebugLogging.action();
        return bot;
    }
}
