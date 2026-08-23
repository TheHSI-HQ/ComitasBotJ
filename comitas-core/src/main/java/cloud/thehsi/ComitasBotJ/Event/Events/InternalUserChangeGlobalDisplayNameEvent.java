package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserChangeGlobalDisplayNameEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record InternalUserChangeGlobalDisplayNameEvent(User user, @Nullable String oldName,
                                                       @Nullable String newName) implements UserChangeGlobalDisplayNameEvent {
    @Override
    public @NotNull User getUser() {
        DebugLogging.action();
        return user;
    }

    @Override
    public @Nullable String getNewDisplayName() {
        DebugLogging.action();
        return newName;
    }

    @Override
    public @Nullable String getOldDisplayName() {
        DebugLogging.action();
        return oldName;
    }
}
