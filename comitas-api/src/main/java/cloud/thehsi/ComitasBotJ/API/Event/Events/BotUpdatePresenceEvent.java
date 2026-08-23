package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.Activity;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.OnlineStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface BotUpdatePresenceEvent extends Event, UndoableEvent {
    /**
     * Returns the bot's old activty if changed
     *
     * @return The old activity if changed
     */
    @Nullable
    Activity getOldActivity();

    /**
     * Returns the bot's current activity
     *
     * @return The current activity
     */
    @Nullable
    Activity getNewActivity();

    /**
     * Returns the bot's old online status if changed
     *
     * @return The old online status if changed
     */
    @Nullable
    OnlineStatus getOldStatus();

    /**
     * Returns the bot's current online status
     *
     * @return The current online status
     */
    @NotNull
    OnlineStatus getNewStatus();

    /**
     * Returns the bot
     *
     * @return The bot
     */
    @NotNull
    Bot getBot();
}