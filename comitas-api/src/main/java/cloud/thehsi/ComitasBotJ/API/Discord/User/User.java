package cloud.thehsi.ComitasBotJ.API.Discord.User;

public class User {
    private final InternalUserImpl impl;

    public User(InternalUserImpl impl) {
        this.impl = impl;
    }

    public String getUsername() {
        return impl.getUsername();
    }

    public String getDisplayName() {
        return impl.getDisplayName();
    }

    public void sendDirectMessage(String message) {
        impl.sendDirectMessage(message);
    }
}
