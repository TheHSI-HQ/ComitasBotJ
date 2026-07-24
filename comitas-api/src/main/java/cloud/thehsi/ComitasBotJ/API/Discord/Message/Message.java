package cloud.thehsi.ComitasBotJ.API.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public interface Message {
    /**
     * Delete the message
     */
    void delete();

    /**
     * Checks if the message is deleted
     */
    boolean isDeleted();

    /**
     * Get the messages raw content
     *
     * @return The raw message content
     */
    String getRawContent();

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
    TextChannel getChannel();

    /**
     * Is this message a reply to another message
     *
     * @return Is this a reply
     */
    boolean isReply();

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
     * Get a list of attachments to this message
     *
     * @return The list of attachments
     */
    List<Attachment> getAttachments();

    /**
     * Reply to the message in the same channel this message was send in
     *
     * @param message The message to be sent
     */
    void reply(Component message);

    /**
     * Reply to the message in the same channel this message was send in with an Embed
     *
     * @param message The message to be sent
     * @param embed The embed to attach to this message
     */
    void reply(Component message, Embed embed);

    /**
     * Reply to the message in the same channel this message was send in with multiple Embeds
     *
     * @param message The message to be sent
     * @param embeds The embeds to attach to this message
     */
    void reply(Component message, Embed... embeds);
}
