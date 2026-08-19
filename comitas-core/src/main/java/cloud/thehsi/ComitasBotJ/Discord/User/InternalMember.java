package cloud.thehsi.ComitasBotJ.Discord.User;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Ban;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.User.ClientType;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.OnlineStatus;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalBan;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;

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
        return this;
    }

    @Override
    public Guild getGuild() {
        return new InternalGuild(member.getGuild());
    }

    @Override
    public Color getPrimaryColor() {
        return member.getRoles().stream()
                .map(role -> role.getColors().getPrimary())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Color getSecondaryColor() {
        return member.getRoles().stream()
                .map(role -> role.getColors().getSecondary())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public Color getTertiaryColor() {
        return member.getRoles().stream()
                .map(role -> role.getColors().getTertiary())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getLoggableName() {
        Color color = Objects.requireNonNullElse(getPrimaryColor(), new Color(153, 170, 181));

        return ConsoleColor.of(color) + getDisplayName() + ConsoleColor.RESET;
    }

    @Override
    public List<Permission> getPermissions() {
        return member.getPermissions().stream()
                .map(e -> Permission.fromValue(e.name()))
                .toList();
    }

    @Override
    public OnlineStatus getOnlineStatus() {
        return OnlineStatus.fromKey(member.getOnlineStatus().getKey());
    }

    @Override
    public OnlineStatus getOnlineStatus(ClientType clientType) {
        return OnlineStatus.fromKey(member.getOnlineStatus(
                net.dv8tion.jda.api.entities.ClientType.fromKey(clientType.getKey())
        ).getKey());
    }

    @Override
    public void kick() {
        member.kick().complete();
    }

    @Override
    public void kick(String reason) {
        member.kick().reason(reason).complete();
    }

    @Override
    public Ban ban() {
        member.ban(0, TimeUnit.SECONDS).complete();

        return new InternalBan(getUser(), null, getGuild());
    }

    @Override
    public Ban ban(String reason) {
        member.ban(0, TimeUnit.SECONDS).reason(reason).complete();

        return new InternalBan(getUser(), reason, getGuild());
    }

    @Override
    public Ban ban(int deletionPeriodHours) {
        member.ban(deletionPeriodHours, TimeUnit.HOURS).complete();

        return new InternalBan(getUser(), null, getGuild());
    }

    @Override
    public Ban ban(String reason, int deletionPeriodHours) {
        member.ban(deletionPeriodHours, TimeUnit.HOURS).reason(reason).complete();

        return new InternalBan(getUser(), reason, getGuild());
    }
}
