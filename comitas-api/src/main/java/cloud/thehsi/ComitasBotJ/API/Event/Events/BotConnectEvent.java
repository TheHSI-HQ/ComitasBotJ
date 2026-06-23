package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Event.Events.InternalImpl.InternalBotConnectEventImpl;

@SuppressWarnings("unused")
public class BotConnectEvent extends Event {
    private final InternalBotConnectEventImpl impl;

    public BotConnectEvent(InternalBotConnectEventImpl impl) {
        this.impl = impl;
    }

    /**
     * Returns the bot's Username.
     *
     * @return The bot's Username.
     */
    public String getUserName() {
        return impl.getUserName();
    }

    /**
     * Returns the bot's Display Name.
     *
     * @return The bot's Display Name
     */
    public String getDisplayName() {
        return impl.getDisplayName();
    }

    /**
     * Returns the bot's ID.
     *
     * @return The bot's ID
     */
    public Long getId() {
        return impl.getId();
    }
}