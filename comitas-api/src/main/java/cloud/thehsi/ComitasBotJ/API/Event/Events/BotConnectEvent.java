package cloud.thehsi.ComitasBotJ.API.Event.Events;

@SuppressWarnings("unused")
public interface BotConnectEvent extends Event {
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
}