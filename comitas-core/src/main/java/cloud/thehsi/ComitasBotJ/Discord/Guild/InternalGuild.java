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
import cloud.thehsi.ComitasBotJ.Discord.Channel.ChannelTypeResolver;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalMessageChannel;
import cloud.thehsi.ComitasBotJ.Discord.Role.InternalRole;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record InternalGuild(net.dv8tion.jda.api.entities.Guild guild) implements Guild {
    @Override
    public @NotNull String getName() {
        DebugLogging.action();
        return guild.getName();
    }

    @Override
    public long getId() {
        DebugLogging.action();
        return guild.getIdLong();
    }

    @Override
    public @Nullable Channel getChannelById(long id) {
        DebugLogging.action();
        return getChannelById(String.valueOf(id));
    }

    @Override
    public @Nullable Channel getChannelById(@NotNull String id) {
        DebugLogging.action();
        GuildChannel channel = guild.getChannelById(GuildChannel.class, id);
        if (channel == null) return null;
        return ChannelTypeResolver.resolve(channel);
    }

    @Override
    public @Nullable MessageChannel getDefaultChannel() {
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
    public @Nullable Member getMember(@NotNull User user) {
        DebugLogging.action(user);
        net.dv8tion.jda.api.entities.User iUser = ((InternalUser) user).user;
        net.dv8tion.jda.api.entities.Member iMember = guild.getMember(iUser);

        return iMember == null ? null : new InternalMember(iMember);
    }

    @Override
    public @NotNull List<Invite> getInvites() {
        DebugLogging.action();
        return guild.retrieveInvites().complete().stream()
                .map(e -> (Invite) new InternalInvite(e))
                .toList();
    }

    @Override
    public @NotNull List<Ban> getBans() {
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
    public @NotNull List<Member> getMembers() {
        DebugLogging.action();
        return guild.getMembers().stream()
                .map(e -> (Member) new InternalMember(e))
                .toList();
    }

    @Override
    public @NotNull List<Role> getRoles() {
        DebugLogging.action();
        return guild.getRoles().stream()
                .map(e -> (Role) new InternalRole(e))
                .toList();
    }

    @Override
    @Nullable
    public Role getRoleById(long id) {
        net.dv8tion.jda.api.entities.Role role = guild.getRoleById(id);
        if (role == null)
            return null;
        return new InternalRole(role);
    }

    @Override
    @Nullable
    public Role getRoleById(@NotNull String id) {
        DebugLogging.action(id);
        net.dv8tion.jda.api.entities.Role role = guild.getRoleById(id);
        if (role == null)
            return null;
        return new InternalRole(role);
    }

    @Override
    public int getMemberCount() {
        DebugLogging.action();
        return guild.getMemberCount();
    }

    @Override
    public @Nullable String getDescription() {
        DebugLogging.action();
        return guild().getDescription();
    }

    @Override
    public boolean isCommunity() {
        DebugLogging.action();
        return guild.getFeatures().contains("COMMUNITY");
    }

    @Override
    public @NotNull List<Channel> getChannels() {
        DebugLogging.action();
        return guild.getChannels().stream()
                .map(ChannelTypeResolver::resolve)
                .toList();
    }

    @Override
    public void kick(@NotNull Member member) {
        DebugLogging.action(member);
        member.kick();
    }

    @Override
    public void kick(@NotNull Member member, @Nullable String reason) {
        DebugLogging.action(member, reason);
        if (reason == null)
            member.kick();
        else
            member.kick(reason);
    }

    @Override
    public @NotNull Ban ban(@NotNull Member member) {
        DebugLogging.action(member);
        return member.ban();
    }

    @Override
    public @NotNull Ban ban(@NotNull Member member, @Nullable String reason) {
        DebugLogging.action(member, reason);
        if (reason == null)
            return member.ban();
        return member.ban(reason);
    }

    @Override
    public @NotNull Ban ban(@NotNull Member member, @Nullable Integer deletionPeriodHours) {
        DebugLogging.action(member, deletionPeriodHours);
        if (deletionPeriodHours == null)
            return member.ban();
        return member.ban(deletionPeriodHours);
    }

    @Override
    public @NotNull Ban ban(@NotNull Member member, @Nullable String reason, @Nullable Integer deletionPeriodHours) {
        DebugLogging.action(member, reason, deletionPeriodHours);
        if (deletionPeriodHours == null && reason == null)
            return member.ban();
        else if (deletionPeriodHours == null)
            return member.ban(reason);
        else if (reason == null)
            return member.ban(deletionPeriodHours);
        return member.ban(reason, deletionPeriodHours);
    }

    @Override
    public void unban(@NotNull User user) {
        DebugLogging.action(user);
        guild.unban(((InternalUser) user).user).complete();
    }

    @Override
    public boolean amIMember() {
        DebugLogging.action();
        return guild().isMember(guild().getJDA().getSelfUser());
    }
}
