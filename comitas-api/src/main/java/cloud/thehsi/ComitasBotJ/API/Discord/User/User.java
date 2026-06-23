package cloud.thehsi.ComitasBotJ.API.Discord.User;

@SuppressWarnings("unused")
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

    public Long getId() {
        return impl.getId();
    }

    public String mention() {
        return impl.mention();
    }

    public void sendDirectMessage(String message) {
        impl.sendDirectMessage(message);
    }
}
