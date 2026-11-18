package cloud.thehsi.ComitasBotJ.API.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.MessageAttachment;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.RequiresBotPermission;
import cloud.thehsi.ComitasBotJ.API.Discord.RequiresBotPermission.Scope;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Represents a send message
 */
@SuppressWarnings("unused")
public interface Message {
    /**
     * Retrieve the message id
     *
     * @return Retrieve the message id
     */
    long getId();

    /**
     * Delete the message
     */
    @RequiresBotPermission(conditional = Permission.MANAGE_MESSAGES)
    void delete();

    /**
     * Checks if the message is deleted
     *
     * @return Has this message been deleted
     */
    @RequiresBotPermission(Permission.READ_MESSAGE_HISTORY)
    boolean isDeleted();

    /**
     * Get the messages raw content
     *
     * @return The raw message content
     */
    @NotNull
    String getRawContent();

    /**
     * Get the messages content parsed into a component
     *
     * @return The message content as a component tree
     */
    @NotNull
    Component getContent();

    /**
     * Get the Message Author User
     *
     * @return The Message Author User
     */
    @NotNull
    User getAuthorUser();

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
    @Nullable
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
     * Is this message a voice message
     * <p>
     * The audio itself is the only attachment of the message,
     * see {@link MessageAttachment#getDuration()} and {@link MessageAttachment#getWaveform()}.
     *
     * @return Is this a voice message
     */
    boolean isVoiceMessage();

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
    @NotNull
    @Unmodifiable
    List<Reaction> getReactions();

    /**
     * React to this message
     *
     * @param emoji Emoji to react with
     */
    @RequiresBotPermission(value = {Permission.ADD_REACTIONS, Permission.READ_MESSAGE_HISTORY}, conditional = Permission.USE_EXTERNAL_EMOJIS)
    void react(@NotNull Emoji emoji);

    /**
     * Removes a reaction to this message
     *
     * @param emoji Emoji to unreact from
     */
    @RequiresBotPermission(Permission.READ_MESSAGE_HISTORY)
    void unreact(@NotNull Emoji emoji);

    /**
     * Get a list of attachments to this message
     *
     * @return The list of attachments
     */
    @NotNull
    @Unmodifiable
    List<MessageAttachment> getAttachments();

    /**
     * Returns the MessageData of the message
     *
     * @return The MessageData
     */
    @NotNull
    MessageData getData();

    /**
     * Returns an array of all embeds of the message
     *
     * @return The embeds
     */
    @NotNull
    List<Embed> getEmbeds();

    /**
     * Forward the message to a {@link MessageChannel}
     *
     * @param channel Where to forward the message to
     * @return The message with the forwarded message
     */
    @RequiresBotPermission(value = Permission.SEND_MESSAGES, scope = Scope.TARGET_CHANNEL)
    @Nullable
    MyMessage forward(@NotNull MessageChannel channel);

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
    @RequiresBotPermission(value = {Permission.SEND_MESSAGES, Permission.READ_MESSAGE_HISTORY}, conditional = {Permission.MENTION_EVERYONE, Permission.USE_EXTERNAL_EMOJIS})
    @NotNull
    MyMessage reply(@NotNull Component message);

    /**
     * Reply to the message in the same channel this message was send in with message data
     *
     * @param messageData The message data
     * @return The message that was sent
     */
    @RequiresBotPermission(value = {Permission.SEND_MESSAGES, Permission.READ_MESSAGE_HISTORY}, conditional = {Permission.EMBED_LINKS, Permission.ATTACH_FILES, Permission.SEND_VOICE_MESSAGES, Permission.MENTION_EVERYONE, Permission.USE_EXTERNAL_EMOJIS})
    @NotNull
    MyMessage reply(@NotNull MessageData messageData);
}
