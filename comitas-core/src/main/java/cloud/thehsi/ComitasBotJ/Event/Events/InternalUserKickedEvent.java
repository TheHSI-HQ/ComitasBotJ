package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserKickedEvent;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record InternalUserKickedEvent(@NotNull InternalUser user, @Nullable InternalMember moderator,
                                      @NotNull InternalGuild guild) implements UserKickedEvent {
    @Override
    public @NotNull User getUser() {
        DebugLogging.action();
        return user;
    }

    @Override
    public @Nullable Member getModerator() {
        DebugLogging.action();
        return moderator;
    }

    @Override
    public @NotNull Guild getGuild() {
        DebugLogging.action();
        return guild;
    }
}
