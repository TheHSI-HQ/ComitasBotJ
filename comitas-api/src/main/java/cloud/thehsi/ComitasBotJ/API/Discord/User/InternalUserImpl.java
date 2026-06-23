package cloud.thehsi.ComitasBotJ.API.Discord.User;

public interface InternalUserImpl {
    String getUsername();

    String getDisplayName();

    void sendDirectMessage(String Message);
}
