package cloud.thehsi.ComitasBotJ.API.Event.Events;

public interface InternalMessageEventImpl {
    boolean isDelete();

    void setDelete(boolean delete);

    void deleteMessage();

    String getRawContent();

    void sendInChannel(String message);

    void replyToMessage(String message);
}