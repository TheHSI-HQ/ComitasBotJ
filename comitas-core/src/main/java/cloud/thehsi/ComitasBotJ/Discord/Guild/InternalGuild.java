package cloud.thehsi.ComitasBotJ.Discord.Guild;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Ban;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Invite;
import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalChannel;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalForumChannel;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalMessageChannel;
import cloud.thehsi.ComitasBotJ.Discord.Role.InternalRole;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record InternalGuild(net.dv8tion.jda.api.entities.Guild guild) implements Guild {
    @Override
    public String getName() {
        DebugLogging.action();
        return guild.getName();
    }

    @Override
    public Long getId() {
        DebugLogging.action();
        return guild.getIdLong();
    }

    @Override
    public MessageChannel getDefaultChannel() {
        DebugLogging.action();
        net.dv8tion.jda.api.entities.channel.middleman.GuildChannel defaultChannel = guild.getDefaultChannel();

        if (defaultChannel instanceof net.dv8tion.jda.api.entities.channel.middleman.MessageChannel) {
            return new InternalMessageChannel(
                    (net.dv8tion.jda.api.entities.channel.middleman.MessageChannel) guild.getDefaultChannel()
            );
        }

        return null;
    }

    @Override
    public @Nullable String getIconUrl() {
        DebugLogging.action();
        return guild.getIconUrl();
    }

    @Override
    public @Nullable String getBannerUrl() {
        DebugLogging.action();
        return guild.getBannerUrl();
    }

    @Override
    public @Nullable Member getMember(User user) {
        DebugLogging.action(user);
        net.dv8tion.jda.api.entities.User iUser = ((InternalUser) user).user;
        net.dv8tion.jda.api.entities.Member iMember = guild.getMember(iUser);

        return iMember == null ? null : new InternalMember(iMember);
    }

    @Override
    public List<Invite> getInvites() {
        DebugLogging.action();
        return guild.retrieveInvites().complete().stream()
                .map(e -> (Invite) new InternalInvite(e))
                .toList();
    }

    @Override
    public List<Ban> getBans() {
        DebugLogging.action();
        return guild.retrieveBanList().complete().stream()
                .map(e -> (Ban) (new InternalBan(
                        new InternalUser(e.getUser()),
                        e.getReason(),
                        this
                )))
                .toList();
    }

    @Override
    public List<Member> getMembers() {
        DebugLogging.action();
        return guild.getMembers().stream()
                .map(e -> (Member) new InternalMember(e))
                .toList();
    }

    @Override
    public List<Role> getRoles() {
        DebugLogging.action();
        return guild.getRoles().stream()
                .map(e -> (Role) new InternalRole(e))
                .toList();
    }

    @Override
    public int getMemberCount() {
        DebugLogging.action();
        return guild.getMemberCount();
    }

    @Override
    public String getDescription() {
        DebugLogging.action();
        return guild().getDescription();
    }

    @Override
    public boolean isCommunity() {
        DebugLogging.action();
        return guild.getFeatures().contains("COMMUNITY");
    }

    @Override
    public List<Channel> getChannels() {
        DebugLogging.action();
        return guild.getChannels().stream()
                .map(channel -> {
                    // Channel Mapping
                    if (channel instanceof ForumChannel cast)
                        return new InternalForumChannel(cast);
                    if (channel instanceof net.dv8tion.jda.api.entities.channel.middleman.MessageChannel cast)
                        return new InternalMessageChannel(cast);

                    return new InternalChannel(channel);
                }).map(e -> (Channel) e)
                .toList();
    }

    @Override
    public void kick(Member member) {
        DebugLogging.action(member);
        member.kick();
    }

    @Override
    public void kick(Member member, String reason) {
        DebugLogging.action(member, reason);
        member.kick(reason);
    }

    @Override
    public Ban ban(Member member) {
        DebugLogging.action(member);
        return member.ban();
    }

    @Override
    public Ban ban(Member member, String reason) {
        DebugLogging.action(member, reason);
        return member.ban(reason);
    }

    @Override
    public Ban ban(Member member, int deletionPeriodHours) {
        DebugLogging.action(member, deletionPeriodHours);
        return member.ban(deletionPeriodHours);
    }

    @Override
    public Ban ban(Member member, String reason, int deletionPeriodHours) {
        DebugLogging.action(member, reason, deletionPeriodHours);
        return member.ban(reason, deletionPeriodHours);
    }

    @Override
    public void unban(User user) {
        DebugLogging.action(user);
        guild.unban(((InternalUser) user).user).complete();
    }

    @Override
    public boolean amIMember() {
        DebugLogging.action();
        return guild().isMember(guild().getJDA().getSelfUser());
    }
}
