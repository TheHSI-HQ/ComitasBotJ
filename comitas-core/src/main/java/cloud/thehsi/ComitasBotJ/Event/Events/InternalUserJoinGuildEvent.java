package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserJoinGuildEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

public record InternalUserJoinGuildEvent(GuildMemberJoinEvent event) implements UserJoinGuildEvent {
    @Override
    public Member getMember() {
        DebugLogging.action();
        return new InternalMember(event.getMember());
    }

    @Override
    public Guild getGuild() {
        DebugLogging.action();
        return new InternalGuild(event.getGuild());
    }
}
