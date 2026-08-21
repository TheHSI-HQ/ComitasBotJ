package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserLeaveGuildEvent;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.jetbrains.annotations.Nullable;

public record InternalUserLeaveGuildEvent(GuildMemberRemoveEvent event) implements UserLeaveGuildEvent {
    @Override
    public User getUser() {
        return new InternalUser(event.getUser());
    }

    @Override
    public @Nullable Member getMember() {
        if (event.getMember() == null)
            return null;
        return new InternalMember(event.getMember());
    }

    @Override
    public Guild getGuild() {
        return new InternalGuild(event.getGuild());
    }
}
