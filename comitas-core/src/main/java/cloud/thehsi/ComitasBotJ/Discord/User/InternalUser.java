package cloud.thehsi.ComitasBotJ.Discord.User;

import cloud.thehsi.ComitasBotJ.API.Discord.User.InternalUserImpl;
import net.dv8tion.jda.api.entities.User;

public class InternalUser implements InternalUserImpl {
    private final User user;

    public InternalUser(User user) {
        this.user = user;
    }

    @Override
    public String getUsername() {
        return user.getName();
    }

    @Override
    public String getDisplayName() {
        return user.getEffectiveName();
    }

    @Override
    public Long getId() {
        return user.getIdLong();
    }

    @Override
    public String mention() {
        return "<@" + user.getId() + ">";
    }

    @Override
    public void sendDirectMessage(String Message) {

    }
}
