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
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalMessageChannel;
import cloud.thehsi.ComitasBotJ.Discord.Role.InternalRole;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
        List<Invite> invites = new ArrayList<>();

        for (net.dv8tion.jda.api.entities.Invite invite : guild.retrieveInvites().complete()) {
            invites.add(new InternalInvite(invite));
        }

        return invites;
    }

    @Override
    public List<Ban> getBans() {
        List<Ban> bans = new ArrayList<>();

        for (net.dv8tion.jda.api.entities.Guild.Ban ban : guild.retrieveBanList().complete())
            bans.add(new InternalBan(
                    new InternalUser(ban.getUser()),
                    ban.getReason(),
                    this
            ));

        return bans;
    }

    @Override
    public List<Member> getMembers() {
        List<Member> members = new ArrayList<>();

        for (net.dv8tion.jda.api.entities.Member member : guild.getMembers()) {
            members.add(new InternalMember(member));
        }

        return members;
    }

    @Override
    public List<Role> getRoles() {
        List<Role> roles = new ArrayList<>();

        for (net.dv8tion.jda.api.entities.Role role : guild.getRoles()) {
            roles.add(new InternalRole(role));
        }

        return roles;
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
        List<Channel> channels = new ArrayList<>();

        for (net.dv8tion.jda.api.entities.channel.Channel channel : guild.getChannels()) {
            if (channel instanceof net.dv8tion.jda.api.entities.channel.middleman.MessageChannel)
                channels.add(new InternalMessageChannel((net.dv8tion.jda.api.entities.channel.middleman.MessageChannel) channel));
            else
                channels.add(new InternalChannel(channel));
        }

        return channels;
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
