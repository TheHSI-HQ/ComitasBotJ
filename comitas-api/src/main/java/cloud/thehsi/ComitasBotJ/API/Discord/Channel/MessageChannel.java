package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageHistory;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface MessageChannel extends Channel {
    /**
     * Send a Message in the Channel
     *
     * @param message The message to be sent
     * @return The message that was sent
     */
    @NotNull
    MyMessage sendMessage(@NotNull Component message);

    /**
     * Send a Message in the Channel using message data
     *
     * @param messageData The message data to be sent
     * @return The message that was sent
     */
    @NotNull
    MyMessage sendMessage(@NotNull MessageData messageData);

    /**
     * Retrieve a list of every message sent in this channel
     *
     * @return A list of all message in this channel
     */
    @NotNull
    MessageHistory getMessageHistory();

    /**
     * Get a message sent in this channel by its id
     *
     * @return The requested message or null
     */
    @Nullable
    Message getMessageById(long id);

    /**
     * Get a message sent in this channel by its id
     *
     * @return The requested message or null
     */
    @Nullable
    Message getMessageById(@NotNull String id);

    /**
     * Gets the channel's guild
     *
     * @return The channel's guild if present
     */
    @Nullable
    Guild getGuild();
}
