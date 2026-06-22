package cloud.thehsi.ComitasBotJ.API.Event.Events;

public class MessageEvent extends Event {
    private final InternalMessageEventImpl impl;

    public MessageEvent(InternalMessageEventImpl impl) {
        this.impl = impl;
    }

    public boolean isDelete() {
        return impl.isDelete();
    }

    public void setDelete(boolean delete) {
        impl.setDelete(delete);
    }

    public void deleteMessage() {
        impl.deleteMessage();
    }

    public String getRawContent() {
        return impl.getRawContent();
    }

    public void sendInChannel(String message) {
        impl.sendInChannel(message);
    }

    public void replyToMessage(String message) {
        impl.replyToMessage(message);
    }
}
