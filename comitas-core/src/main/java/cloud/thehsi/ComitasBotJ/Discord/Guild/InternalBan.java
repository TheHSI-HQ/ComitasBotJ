package cloud.thehsi.ComitasBotJ.Discord.Guild;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Ban;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record InternalBan(User user, @Nullable String reason, Guild guild) implements Ban {
    @Override
    public void unban() {
        DebugLogging.action();
        getGuild().unban(user);
    }

    @Override
    public User getUser() {
        DebugLogging.action();
        return user;
    }

    @Override
    public @Nullable String getReason() {
        DebugLogging.action();
        return reason;
    }

    @Override
    public Guild getGuild() {
        DebugLogging.action();
        return guild;
    }

    @Override
    public @NotNull String toString() {
        return "InternalBan[" +
                "user=" + user + ", " +
                "reason=" + reason + ", " +
                "guild=" + guild + ']';
    }

}
