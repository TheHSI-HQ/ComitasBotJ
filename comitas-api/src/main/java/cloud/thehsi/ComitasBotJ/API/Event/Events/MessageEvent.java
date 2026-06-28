package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;

@SuppressWarnings("unused")
public interface MessageEvent extends Event {
    /**
     * Is this message marked for deletion
     *
     * @return The messages deletion status
     */
    boolean isDelete();

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
     * Reply to the message in the same channel this message was send in
     *
     * @param message The message to be sent
     */
    void reply(String message);
}
