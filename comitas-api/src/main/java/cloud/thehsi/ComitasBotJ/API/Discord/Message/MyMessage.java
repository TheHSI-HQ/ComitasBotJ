package cloud.thehsi.ComitasBotJ.API.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.RequiresBotPermission;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a message send by the bot
 */
@SuppressWarnings("unused")
public interface MyMessage extends Message {
    /**
     * Set the content of this message
     *
     * @param content The new message content
     */
    @RequiresBotPermission(conditional = {Permission.MENTION_EVERYONE, Permission.USE_EXTERNAL_EMOJIS})
    void setContent(@NotNull Component content);

    /**
     * Set the content of this message
     *
     * @param messageData The new message data
     */
    @RequiresBotPermission(conditional = {Permission.EMBED_LINKS, Permission.ATTACH_FILES, Permission.MENTION_EVERYONE, Permission.USE_EXTERNAL_EMOJIS})
    void setMessageData(@NotNull MessageData messageData);
}
