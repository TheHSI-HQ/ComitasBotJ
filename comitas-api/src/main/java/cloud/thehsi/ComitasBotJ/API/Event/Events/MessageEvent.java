package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.API.Event.Events.InternalImpl.InternalMessageEventImpl;

@SuppressWarnings("unused")
public class MessageEvent extends Event {
    private final InternalMessageEventImpl impl;

    public MessageEvent(InternalMessageEventImpl impl) {
        this.impl = impl;
    }

    /**
     * Is this message marked for deletion
     *
     * @return The messages deletion status
     */
    public boolean isDelete() {
        return impl.isDelete();
    }

    /**
     * Mark / Unmark the message for deletion
     *
     * @param delete Set the message's marked for deletion status
     */
    public void setDelete(boolean delete) {
        impl.setDelete(delete);
    }

    /**
     * Mark the message for deletion
     */
    public void deleteMessage() {
        impl.deleteMessage();
    }

    /**
     * Get the messages raw content
     *
     * @return The raw message content
     */
    public String getRawContent() {
        return impl.getRawContent();
    }

    /**
     * Get the Message Author
     *
     * @return The Message Author
     */
    public User getAuthor() {
        return impl.getAuthor();
    }

    /**
     * Send a new Message in the same channel this message was send in
     *
     * @param message The message to be sent
     */
    public void sendInChannel(String message) {
        impl.sendInChannel(message);
    }

    /**
     * Reply to the message in the same channel this message was send in
     *
     * @param message The message to be sent
     */
    public void replyToMessage(String message) {
        impl.replyToMessage(message);
    }
}
