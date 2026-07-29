package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;

@SuppressWarnings("unused")
public interface BotReadyEvent extends Event {
    /**
     * Returns the bot's Username.
     *
     * @return The bot's Username.
     */
    String getUserName();

    /**
     * Returns the bot's Display Name.
     *
     * @return The bot's Display Name
     */
    String getDisplayName();

    /**
     * Returns the bot's ID.
     *
     * @return The bot's ID
     */
    Long getId();

    /**
     * Returns the bot
     *
     * @return The bot
     */
    Bot getBot();
}