package cloud.thehsi.ComitasBotJ.API.Discord.User;

public interface InternalUserImpl {
    String getUserName();
    String getDisplayName();

    Long getId();

    boolean isBot();

    boolean isMe();

    String mention();

    boolean sendDirectMessage(String Message);
}
