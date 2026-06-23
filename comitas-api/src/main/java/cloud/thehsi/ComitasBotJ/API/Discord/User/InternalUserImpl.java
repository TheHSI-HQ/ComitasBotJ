package cloud.thehsi.ComitasBotJ.API.Discord.User;

public interface InternalUserImpl {
    String getUsername();
    String getDisplayName();

    Long getId();

    String mention();

    void sendDirectMessage(String Message);
}
