package cloud.thehsi.ComitasBotJ.Discord.Guild;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Invite;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalChannel;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

public record InternalInvite(net.dv8tion.jda.api.entities.Invite invite) implements Invite {
    @Override
    public void delete() {
        DebugLogging.action();
        invite().delete().complete();
    }

    @Override
    @Nullable
    public Channel getChannel() {
        DebugLogging.action();
        net.dv8tion.jda.api.entities.Invite.Channel channel = invite().getChannel();
        if (channel == null)
            return null;

        return new InternalChannel((net.dv8tion.jda.api.entities.channel.Channel) channel);
    }

    @Override
    public @NotNull Guild getGuild() {
        DebugLogging.action();
        return new InternalGuild((net.dv8tion.jda.api.entities.Guild) invite().getGuild());
    }

    @Override
    public @NotNull String getCode() {
        DebugLogging.action();
        return invite().getCode();
    }

    @Override
    public @NotNull String getUrl() {
        DebugLogging.action();
        return invite().getUrl();
    }

    @Override
    public @Nullable User getInviter() {
        DebugLogging.action();
        net.dv8tion.jda.api.entities.User invtter = invite().getInviter();
        if (invtter == null)
            return null;
        return new InternalUser(invtter);
    }

    @Override
    public int getMaxAge() {
        DebugLogging.action();
        return invite().getMaxAge();
    }

    @Override
    public int getMaxUses() {
        DebugLogging.action();
        return invite().getMaxUses();
    }

    @Override
    public @NotNull OffsetDateTime getTimeCreated() {
        DebugLogging.action();
        return invite().getTimeCreated();
    }

    @Override
    public @NotNull OffsetDateTime getExpiryTime() {
        DebugLogging.action();
        return invite().getTimeCreated().plusSeconds(invite.getMaxAge());
    }

    @Override
    public int getUses() {
        DebugLogging.action();
        return invite().getUses();
    }
}
