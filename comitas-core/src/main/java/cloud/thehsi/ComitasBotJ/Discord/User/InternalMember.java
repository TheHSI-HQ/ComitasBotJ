package cloud.thehsi.ComitasBotJ.Discord.User;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Ban;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.ClientType;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.OnlineStatus;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalBan;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Role.InternalRole;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class InternalMember extends InternalUser implements Member {
    public final net.dv8tion.jda.api.entities.Member member;

    public InternalMember(net.dv8tion.jda.api.entities.Member member) {
        super(member.getUser());
        this.member = member;
    }

    @Override
    public String toString() {
        return "InternalMember{" +
                "member=" + member +
                '}';
    }

    @Override
    public User getUser() {
        DebugLogging.action();
        return this;
    }

    @Override
    public Guild getGuild() {
        DebugLogging.action();
        return new InternalGuild(member.getGuild());
    }

    @Override
    public Color getPrimaryColor() {
        DebugLogging.action();
        return member.getRoles().stream()
                .map(role -> role.getColors().getPrimary())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Color getSecondaryColor() {
        DebugLogging.action();
        return member.getRoles().stream()
                .map(role -> role.getColors().getSecondary())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Color getTertiaryColor() {
        DebugLogging.action();
        return member.getRoles().stream()
                .map(role -> role.getColors().getTertiary())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getLoggableName() {
        DebugLogging.action();
        Color color = Objects.requireNonNullElse(getPrimaryColor(), new Color(153, 170, 181));

        return ConsoleColor.of(color) + getDisplayName() + ConsoleColor.RESET;
    }

    @Override
    public List<Permission> getPermissions() {
        DebugLogging.action();
        return member.getPermissions().stream()
                .map(e -> Permission.fromValue(e.name()))
                .toList();
    }

    @Override
    public OnlineStatus getOnlineStatus() {
        DebugLogging.action();
        return OnlineStatus.fromKey(member.getOnlineStatus().getKey());
    }

    @Override
    public OnlineStatus getOnlineStatus(ClientType clientType) {
        DebugLogging.action(clientType);
        return OnlineStatus.fromKey(member.getOnlineStatus(
                net.dv8tion.jda.api.entities.ClientType.fromKey(clientType.getKey())
        ).getKey());
    }

    @Override
    public void addRole(Role role) {
        DebugLogging.action(role);
        if (!(role instanceof InternalRole(net.dv8tion.jda.api.entities.Role iRole)))
            throw new IllegalArgumentException("Role was not created by Comitas");
        if (member.getRoles().stream().anyMatch(e -> e.getIdLong() == iRole.getIdLong()))
            return;
        member.getGuild().addRoleToMember(member, iRole).complete();
    }

    @Override
    public void removeRole(Role role) {
        DebugLogging.action(role);
        if (!(role instanceof InternalRole(net.dv8tion.jda.api.entities.Role iRole)))
            throw new IllegalArgumentException("Role was not created by Comitas");
        if (member.getRoles().stream().noneMatch(e -> e.getIdLong() == iRole.getIdLong()))
            return;
        member.getGuild().removeRoleFromMember(member, iRole).complete();
    }

    @Override
    public boolean hasRole(Role role) {
        DebugLogging.action(role);
        if (!(role instanceof InternalRole(net.dv8tion.jda.api.entities.Role iRole)))
            throw new IllegalArgumentException("Role was not created by Comitas");
        return member.getRoles().stream().noneMatch(e -> e.getIdLong() == iRole.getIdLong());
    }

    @Override
    public List<Role> getRoles() {
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
    public void kick(String reason) {
        DebugLogging.action(reason);
        member.kick().reason(reason).complete();
    }

    @Override
    public Ban ban() {
        DebugLogging.action();
        member.ban(0, TimeUnit.SECONDS).complete();

        return new InternalBan(getUser(), null, getGuild());
    }

    @Override
    public Ban ban(String reason) {
        DebugLogging.action(reason);
        member.ban(0, TimeUnit.SECONDS).reason(reason).complete();

        return new InternalBan(getUser(), reason, getGuild());
    }

    @Override
    public Ban ban(int deletionPeriodHours) {
        DebugLogging.action(deletionPeriodHours);
        member.ban(deletionPeriodHours, TimeUnit.HOURS).complete();

        return new InternalBan(getUser(), null, getGuild());
    }

    @Override
    public Ban ban(String reason, int deletionPeriodHours) {
        DebugLogging.action(reason, deletionPeriodHours);
        member.ban(deletionPeriodHours, TimeUnit.HOURS).reason(reason).complete();

        return new InternalBan(getUser(), reason, getGuild());
    }
}
