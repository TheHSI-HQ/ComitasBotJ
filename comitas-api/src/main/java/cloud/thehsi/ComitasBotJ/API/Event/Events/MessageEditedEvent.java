package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface MessageEditedEvent extends Event {
    /**
     * Is this message marked for deletion
     *
     * @return The messages deletion status
     */
    boolean markedForDeletion();

    /**
     * Mark / Unmark the message for deletion
     *
     * @param delete Set the message's marked for deletion status
     */
    void setDelete(boolean delete);

    /**
     * Mark the message for deletion
     */
    void deleteMessage();

    /**
     * Get the messages raw content
     *
     * @return The raw message content
     */
    String getRawContent();

    /**
     * Get the messages content parsed into a component
     *
     * @return The message content as a component tree
     */
    Component getContent();

    /**
     * Get the Message
     *
     * @return The Message
     */
    Message getMessage();

    /**
     * Get the Message Author
     *
     * @return The Message Author
     */
    @Nullable
    Member getAuthor();

    /**
     * Get the Channel the Message was sent in
     *
     * @return The Message Channel
     */
    MessageChannel getChannel();

    /**
     * Get the Guild the Message was sent in
     *
     * @return The Message Guild (Maybe null if dm)
     */
    @Nullable
    Guild getGuild();

    /**
     * Reply to the message in the same channel this message was send in
     *
     * @param message The message to be sent
     * @return The message that was sent
     */
    MyMessage reply(Component message);

    /**
     * Reply to the message in the same channel this message was send in with message data
     *
     * @param messageData The message data
     * @return The message that was sent
     */
    MyMessage reply(MessageData messageData);
}
