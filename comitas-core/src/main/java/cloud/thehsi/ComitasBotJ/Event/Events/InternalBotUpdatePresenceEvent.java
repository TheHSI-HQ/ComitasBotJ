package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.Activity;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.OnlineStatus;
import cloud.thehsi.ComitasBotJ.API.Event.EventOrigin;
import cloud.thehsi.ComitasBotJ.API.Event.Events.BotUpdatePresenceEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InternalBotUpdatePresenceEvent extends InternalUndoableEvent implements BotUpdatePresenceEvent {
    @Nullable
    final Activity oldActivity;
    @Nullable
    final Activity newActivity;
    @Nullable
    final OnlineStatus oldStatus;
    @NotNull
    final OnlineStatus newStatus;
    @NotNull
    final Bot bot;
    @NotNull
    final EventOrigin eventOrigin;

    public InternalBotUpdatePresenceEvent(@Nullable Activity oldActivity, @Nullable Activity newActivity, @Nullable OnlineStatus oldStatus, @NotNull OnlineStatus newStatus, @NotNull Bot bot, @NotNull EventOrigin origin) {
        this.oldActivity = oldActivity;
        this.newActivity = newActivity;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.bot = bot;
        this.eventOrigin = origin;
    }

    @Override
    public @Nullable Activity getOldActivity() {
        DebugLogging.action();
        return oldActivity;
    }

    @Override
    public @Nullable Activity getNewActivity() {
        DebugLogging.action();
        return newActivity;
    }

    @Override
    public @Nullable OnlineStatus getOldStatus() {
        DebugLogging.action();
        return oldStatus;
    }

    @Override
    public @NotNull OnlineStatus getNewStatus() {
        DebugLogging.action();
        return newStatus;
    }

    @Override
    public @NotNull Bot getBot() {
        DebugLogging.action();
        return bot;
    }

    @Override
    public @NotNull EventOrigin getOrigin() {
        DebugLogging.action();
        return eventOrigin;
    }
}
