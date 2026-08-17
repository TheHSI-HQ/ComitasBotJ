package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface MessageChannel extends Channel {
    /**
     * Send a Message in the Channel
     *
     * @param message The message to be sent
     * @return The message that was sent
     */
    MyMessage sendMessage(Component message);

    /**
     * Send a Message in the Channel using message data
     *
     * @param messageData The message data to be sent
     * @return The message that was sent
     */
    MyMessage sendMessage(MessageData messageData);

    /**
     * Gets the channel's guild
     *
     * @return The channel's guild if present
     */
    @Nullable
    Guild getGuild();
}
