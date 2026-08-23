package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface BotReadyEvent extends Event {
    /**
     * Returns the bot's Username.
     *
     * @return The bot's Username.
     */
    @NotNull
    String getUserName();

    /**
     * Returns the bot's Display Name.
     *
     * @return The bot's Display Name
     */
    @NotNull
    String getDisplayName();

    /**
     * Returns the bot's ID.
     *
     * @return The bot's ID
     */
    long getId();

    /**
     * Returns the bot
     *
     * @return The bot
     */
    @NotNull
    Bot getBot();
}