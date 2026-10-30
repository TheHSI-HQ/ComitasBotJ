package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import org.jetbrains.annotations.NotNull;

/**
 * The event fired when the Bot is ready, after all plugins got enabled.
 * <p>
 * Only fired when the bot (re-)connects to Discord, not on soft reloads.
 *
 * @deprecated Plugins are enabled once the bot is connected, use
 * {@link cloud.thehsi.ComitasBotJ.API.Plugin.Plugin#onEnable()} for setup instead.
 * This event may be removed in a future version.
 */
@Deprecated(forRemoval = true)
@SuppressWarnings({"unused", "DeprecatedIsStillUsed"})
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