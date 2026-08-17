package cloud.thehsi.ComitasBotJ.Discord.Guild;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Ban;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Invite;
import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
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
        return guild.getName();
    }

    @Override
    public Long getId() {
        return guild.getIdLong();
    }

    @Override
    public MessageChannel getDefaultChannel() {
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
        return guild.getIconUrl();
    }

    @Override
    public @Nullable String getBannerUrl() {
        return guild.getBannerUrl();
    }

    @Override
    public @Nullable Member getMember(User user) {
        net.dv8tion.jda.api.entities.User iUser = ((InternalUser) user).user;
        net.dv8tion.jda.api.entities.Member iMember = guild.getMember(iUser);

        return iMember == null ? null : new InternalMember(iMember);
    }

    @Override
    public List<Invite> getInvites() {
        return guild.retrieveInvites().complete().stream()
                .map(e -> (Invite) new InternalInvite(e))
                .toList();
    }

    @Override
    public List<Ban> getBans() {
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
        return guild.getMembers().stream()
                .map(e -> (Member) new InternalMember(e))
                .toList();
    }

    @Override
    public List<Role> getRoles() {
        return guild.getRoles().stream()
                .map(e -> (Role) new InternalRole(e))
                .toList();
    }

    @Override
    public int getMemberCount() {
        return guild.getMemberCount();
    }

    @Override
    public String getDescription() {
        return guild().getDescription();
    }

    @Override
    public boolean isCommunity() {
        return guild.getFeatures().contains("COMMUNITY");
    }

    @Override
    public List<Channel> getChannels() {
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
        member.kick();
    }

    @Override
    public void kick(Member member, String reason) {
        member.kick(reason);
    }

    @Override
    public Ban ban(Member member) {
        return member.ban();
    }

    @Override
    public Ban ban(Member member, String reason) {
        return member.ban(reason);
    }

    @Override
    public Ban ban(Member member, int deletionPeriodHours) {
        return member.ban(deletionPeriodHours);
    }

    @Override
    public Ban ban(Member member, String reason, int deletionPeriodHours) {
        return member.ban(reason, deletionPeriodHours);
    }

    @Override
    public void unban(User user) {
        guild.unban(((InternalUser) user).user).complete();
    }

    @Override
    public boolean amIMember() {
        return guild().isMember(guild().getJDA().getSelfUser());
    }
}
