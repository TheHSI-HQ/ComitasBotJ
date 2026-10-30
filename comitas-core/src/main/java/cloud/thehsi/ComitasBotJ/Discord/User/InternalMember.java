package cloud.thehsi.ComitasBotJ.Discord.User;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.VoiceChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MemberNotInVoiceException;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Ban;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.Activity;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.ActivityType;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.ClientType;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.OnlineStatus;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.BotPermissions;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalVoiceChannel;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalBan;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Role.InternalRole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class InternalMember extends InternalUser implements Member {
    public @NotNull
    final net.dv8tion.jda.api.entities.Member member;

    public InternalMember(@NotNull net.dv8tion.jda.api.entities.Member member) {
        super(member.getUser());
        this.member = member;
    }

    @Override
    @NotNull
    public String toString() {
        return "InternalMember{" +
                "member=" + member +
                '}';
    }

    @Override
    public @NotNull User getUser() {
        DebugLogging.action();
        return this;
    }

    @Override
    public @NotNull Guild getGuild() {
        DebugLogging.action();
        return new InternalGuild(member.getGuild());
    }

    @Override
    @Nullable
    public Color getPrimaryColor() {
        DebugLogging.action();
        return member.getRoles().stream()
                .map(role -> role.getColors().getPrimary())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    @Nullable
    public Color getSecondaryColor() {
        DebugLogging.action();
        return member.getRoles().stream()
                .map(role -> role.getColors().getSecondary())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    @Nullable
    public Color getTertiaryColor() {
        DebugLogging.action();
        return member.getRoles().stream()
                .map(role -> role.getColors().getTertiary())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public @NotNull String getLoggableName() {
        DebugLogging.action();
        Color color = Objects.requireNonNullElse(getPrimaryColor(), new Color(153, 170, 181));

        return ConsoleColor.of(color) + getDisplayName() + ConsoleColor.RESET;
    }

    @Override
    public @NotNull List<Permission> getPermissions() {
        DebugLogging.action();
        return member.getPermissions().stream()
                .map(e -> Permission.fromValue(e.name()))
                .toList();
    }

    @Override
    public @NotNull OnlineStatus getOnlineStatus() {
        DebugLogging.action();
        return OnlineStatus.fromKey(member.getOnlineStatus().getKey());
    }

    @Override
    public @NotNull OnlineStatus getOnlineStatus(@NotNull ClientType clientType) {
        DebugLogging.action(clientType);
        return OnlineStatus.fromKey(member.getOnlineStatus(
                net.dv8tion.jda.api.entities.ClientType.fromKey(clientType.getKey())
        ).getKey());
    }

    @Override
    @Unmodifiable
    public @NotNull List<Activity> getActivities() {
        DebugLogging.action();
        return member.getActivities().stream()
                .map(e -> Activity.of(
                        ActivityType.fromKey(e.getType().getKey()),
                        e.getName(),
                        e.getUrl()
                )).toList();
    }

    @Override
    public void addRole(@NotNull Role role) {
        DebugLogging.action(role);
        if (!(role instanceof InternalRole(net.dv8tion.jda.api.entities.Role iRole)))
            throw new IllegalArgumentException("Role was not created by Comitas");
        if (member.getRoles().stream().anyMatch(e -> e.getIdLong() == iRole.getIdLong()))
            return;
        member.getGuild().addRoleToMember(member, iRole).complete();
    }

    @Override
    public void removeRole(@NotNull Role role) {
        DebugLogging.action(role);
        if (!(role instanceof InternalRole(net.dv8tion.jda.api.entities.Role iRole)))
            throw new IllegalArgumentException("Role was not created by Comitas");
        if (member.getRoles().stream().noneMatch(e -> e.getIdLong() == iRole.getIdLong()))
            return;
        member.getGuild().removeRoleFromMember(member, iRole).complete();
    }

    @Override
    public boolean hasRole(@NotNull Role role) {
        DebugLogging.action(role);
        if (!(role instanceof InternalRole(net.dv8tion.jda.api.entities.Role iRole)))
            throw new IllegalArgumentException("Role was not created by Comitas");
        return member.getRoles().stream().anyMatch(e -> e.getIdLong() == iRole.getIdLong());
    }

    @Override
    public @NotNull List<Role> getRoles() {
        return member.getRoles().stream()
                .map(e -> (Role) new InternalRole(e))
                .toList();
    }

    @Override
    public void kick() {
        DebugLogging.action();
        member.kick().complete();
    }

    @Override
    public void kick(@NotNull String reason) {
        DebugLogging.action(reason);
        member.kick().reason(reason).complete();
    }

    @Override
    public @NotNull Ban ban() {
        DebugLogging.action();
        member.ban(0, TimeUnit.SECONDS).complete();

        return new InternalBan(getUser(), null, getGuild());
    }

    @Override
    public @NotNull Ban ban(@NotNull String reason) {
        DebugLogging.action(reason);
        member.ban(0, TimeUnit.SECONDS).reason(reason).complete();

        return new InternalBan(getUser(), reason, getGuild());
    }

    @Override
    public @NotNull Ban ban(int deletionPeriodHours) {
        DebugLogging.action(deletionPeriodHours);
        member.ban(deletionPeriodHours, TimeUnit.HOURS).complete();

        return new InternalBan(getUser(), null, getGuild());
    }

    @Override
    public @NotNull Ban ban(@NotNull String reason, int deletionPeriodHours) {
        DebugLogging.action(reason, deletionPeriodHours);
        member.ban(deletionPeriodHours, TimeUnit.HOURS).reason(reason).complete();

        return new InternalBan(getUser(), reason, getGuild());
    }

    @Override
    public void setDisplayName(@Nullable String displayName) {
        member.getGuild().modifyNickname(member, displayName).complete();
    }

    @Nullable
    private GuildVoiceState voiceState() {
        // Members of events can be outdated, the cached member has the current voice state
        net.dv8tion.jda.api.entities.Member current = member.getGuild().getMemberById(member.getIdLong());
        return (current != null ? current : member).getVoiceState();
    }

    @NotNull
    private AudioChannel requireVoiceChannel() {
        GuildVoiceState state = voiceState();
        if (state == null || state.getChannel() == null)
            throw new MemberNotInVoiceException(member.getUser().getName() + " isn't connected to a voice channel");
        return state.getChannel();
    }

    @Override
    public @Nullable VoiceChannel getVoiceChannel() {
        DebugLogging.action();
        GuildVoiceState state = voiceState();
        return state == null || state.getChannel() == null ? null : new InternalVoiceChannel(state.getChannel());
    }

    @Override
    public void moveTo(@NotNull VoiceChannel channel) {
        DebugLogging.action(channel);
        if (!(channel instanceof InternalVoiceChannel target))
            throw new IllegalArgumentException("Channel was not created by Comitas");
        if (target.audioChannel.getGuild().getIdLong() != member.getGuild().getIdLong())
            throw new IllegalArgumentException("Members can only be moved within their guild");

        AudioChannel current = requireVoiceChannel();
        BotPermissions.require(member.getGuild(), current, Permission.MOVE_MEMBERS);
        BotPermissions.require(member.getGuild(), target.audioChannel, Permission.MOVE_MEMBERS);
        BotPermissions.require(member.getGuild(), target.audioChannel, Permission.CONNECT);

        member.getGuild().moveVoiceMember(member, target.audioChannel).complete();
    }

    @Override
    public void disconnectFromVoice() {
        DebugLogging.action();
        BotPermissions.require(member.getGuild(), requireVoiceChannel(), Permission.MOVE_MEMBERS);
        member.getGuild().moveVoiceMember(member, null).complete();
    }

    @Override
    public boolean isGuildMuted() {
        DebugLogging.action();
        GuildVoiceState state = voiceState();
        return state != null && state.isGuildMuted();
    }

    @Override
    public void setGuildMuted(boolean muted) {
        DebugLogging.action(muted);
        BotPermissions.require(member.getGuild(), requireVoiceChannel(), Permission.MUTE_MEMBERS);
        member.getGuild().mute(member, muted).complete();
    }

    @Override
    public boolean isGuildDeafened() {
        DebugLogging.action();
        GuildVoiceState state = voiceState();
        return state != null && state.isGuildDeafened();
    }

    @Override
    public void setGuildDeafened(boolean deafened) {
        DebugLogging.action(deafened);
        BotPermissions.require(member.getGuild(), requireVoiceChannel(), Permission.DEAFEN_MEMBERS);
        member.getGuild().deafen(member, deafened).complete();
    }
}
