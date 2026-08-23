package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserChangeUserNameEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import org.jetbrains.annotations.NotNull;

public record InternalUserChangeUserNameEvent(User user, String oldName,
                                              String newName) implements UserChangeUserNameEvent {
    @Override
    public @NotNull User getUser() {
        DebugLogging.action();
        return user;
    }

    @Override
    public @NotNull String getNewUserName() {
        DebugLogging.action();
        return newName;
    }

    @Override
    public @NotNull String getOldUserName() {
        DebugLogging.action();
        return oldName;
    }
}
