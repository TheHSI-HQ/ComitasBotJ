package cloud.thehsi.ComitasBotJ.Discord.Guild;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Invite;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalChannel;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;

import java.time.OffsetDateTime;

public record InternalInvite(net.dv8tion.jda.api.entities.Invite invite) implements Invite {
    @Override
    public void delete() {
        DebugLogging.action();
        invite().delete().complete();
    }

    @Override
    public Channel getChannel() {
        DebugLogging.action();
        return new InternalChannel((net.dv8tion.jda.api.entities.channel.Channel) invite().getChannel());
    }

    @Override
    public Guild getGuild() {
        DebugLogging.action();
        return new InternalGuild((net.dv8tion.jda.api.entities.Guild) invite().getGuild());
    }

    @Override
    public String getCode() {
        DebugLogging.action();
        return invite().getCode();
    }

    @Override
    public String getUrl() {
        DebugLogging.action();
        return invite().getUrl();
    }

    @Override
    public User getInviter() {
        DebugLogging.action();
        return new InternalUser(invite().getInviter());
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
    public OffsetDateTime getTimeCreated() {
        DebugLogging.action();
        return invite().getTimeCreated();
    }

    @Override
    public int getUses() {
        DebugLogging.action();
        return invite().getUses();
    }
}
