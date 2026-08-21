package cloud.thehsi.ComitasBotJ.API.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.MessageAttachment;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public interface Message {
    /**
     * Retrieve the message Id
     *
     * @return Retrieve the message id
     */
    long getId();

    /**
     * Delete the message
     */
    void delete();

    /**
     * Checks if the message is deleted
     *
     * @return Has this message been deleted
     */
    boolean isDeleted();

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
     * Get the Message Author
     *
     * @return The Message Author
     */
    Member getAuthor();

    /**
     * Get the Channel the Message was sent in
     *
     * @return The Message Channel
     */
    MessageChannel getChannel();

    /**
     * Is this message a reply to another message
     *
     * @return Is this a reply
     */
    boolean isReply();

    /**
     * Is this message a forward of another message
     *
     * @return Is this a forward
     */
    boolean isForwarded();

    /**
     * The message this message is a reply to
     *
     * @return The message replied to, may be null
     */
    @Nullable
    Message getRepliedMessage();

    /**
     * Get a list of reactions to this message
     *
     * @return The list of reactions
     */
    List<Reaction> getReactions();

    /**
     * React to this message
     */
    void react(Emoji emoji);

    /**
     * Removes a reaction to this message
     */
    void unreact(Emoji emoji);

    /**
     * Get a list of attachments to this message
     *
     * @return The list of attachments
     */
    List<MessageAttachment> getAttachments();

    /**
     * Returns the MessageData of the message
     *
     * @return The MessageData
     */
    MessageData getData();

    /**
     * Returns an array of all embeds of the message
     *
     * @return The embeds
     */
    Embed[] getEmbeds();

    /**
     * Forward the message to a {@link MessageChannel}
     *
     * @return The message with the forwarded message
     */
    @Nullable
    MyMessage forward(MessageChannel channel);

    /**
     * Returns the MyMessage cast of the Message if the bot send the message
     *
     * @return The MyMessage or null
     */
    @Nullable
    MyMessage asMyMessage();


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
