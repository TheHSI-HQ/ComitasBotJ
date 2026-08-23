package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserKickedEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import org.jetbrains.annotations.NotNull;

public record InternalUserKickedEvent(InternalUser user, InternalGuild guild) implements UserKickedEvent {
    @Override
    public @NotNull User getUser() {
        DebugLogging.action();
        return user;
    }

    @Override
    public @NotNull Guild getGuild() {
        DebugLogging.action();
        return guild;
    }
}
