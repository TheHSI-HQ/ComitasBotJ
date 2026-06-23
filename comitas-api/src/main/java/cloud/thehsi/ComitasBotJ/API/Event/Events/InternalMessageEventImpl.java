package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.User.User;

public interface InternalMessageEventImpl {
    boolean isDelete();

    void setDelete(boolean delete);

    void deleteMessage();

    String getRawContent();

    User getAuthor();

    void sendInChannel(String message);

    void replyToMessage(String message);
}