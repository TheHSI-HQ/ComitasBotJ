package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserChangeUserNameEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;

public record InternalUserChangeUserNameEvent(User user, String oldName,
                                              String newName) implements UserChangeUserNameEvent {
    @Override
    public User getUser() {
        DebugLogging.action();
        return user;
    }

    @Override
    public String getNewUserName() {
        DebugLogging.action();
        return newName;
    }

    @Override
    public String getOldUserName() {
        DebugLogging.action();
        return oldName;
    }
}
