package cloud.thehsi.ComitasBotJ.API.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface MyMessage extends Message {
    /**
     * Set the content of this message
     *
     * @param content The new message content
     */
    void setContent(@NotNull Component content);

    /**
     * Set the content of this message
     *
     * @param messageData The new message data
     */
    void setMessageData(@NotNull MessageData messageData);
}
