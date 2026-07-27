package cloud.thehsi.ComitasBotJ.Discord.User;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentParser;
import cloud.thehsi.ComitasBotJ.Discord.Message.Embeds.InternalEmbed;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMyMessage;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class InternalMember implements Member {
    private final net.dv8tion.jda.api.entities.User user;
    private final net.dv8tion.jda.api.entities.Member member;

    public InternalMember(net.dv8tion.jda.api.entities.Member member) {
        this.user = member.getUser();
        this.member = member;
    }

    @Override
    public String getUserName() {
        return user.getName();
    }

    @Override
    public String getDisplayName() {
        return user.getEffectiveName();
    }

    @Override
    public Long getId() {
        return user.getIdLong();
    }

    @Override
    public boolean isBot() {
        return user.isBot();
    }

    @Override
    public boolean isMe() {
        return user.getIdLong() == user.getJDA().getSelfUser().getIdLong();
    }

    @Override
    public Component mention() {
        return Component.raw(user.getAsMention());
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
        List<Permission> permissions = new ArrayList<>();
        for (net.dv8tion.jda.api.Permission permission : member.getPermissions())
            permissions.add(
                    Permission.fromValue(permission.name())
            );
        return permissions;
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
    public void ban() {
        member.ban(0, TimeUnit.SECONDS).complete();
    }

    @Override
    public void ban(String reason) {
        member.ban(0, TimeUnit.SECONDS).reason(reason).complete();
    }

    @Override
    public void ban(int deletionPeriodHours) {
        member.ban(deletionPeriodHours, TimeUnit.HOURS).complete();
    }

    @Override
    public void ban(String reason, int deletionPeriodHours) {
        member.ban(deletionPeriodHours, TimeUnit.HOURS).reason(reason).complete();
    }

    @Override
    public MyMessage sendDirectMessage(Component message) {
        String msg = ComponentParser.parseComponent(message);

        return new InternalMyMessage(member.getUser().openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(msg))
                .complete());
    }

    @Override
    public MyMessage sendDirectMessage(Component message, Embed embed) {
        String msg = ComponentParser.parseComponent(message);

        if (!(embed instanceof InternalEmbed internal))
            throw new IllegalArgumentException("Embed was not created using the EmbedBuilder");

        MessageEmbed messageEmbed = internal.embed();

        try (MessageCreateData data = new MessageCreateBuilder().setContent(msg).setEmbeds(messageEmbed).build()) {
            return new InternalMyMessage(member.getUser().openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage(data))
                    .complete());
        }
    }

    @Override
    public MyMessage sendDirectMessage(Component message, Embed... embeds) {
        String msg = ComponentParser.parseComponent(message);

        MessageEmbed[] messageEmbeds = new MessageEmbed[embeds.length];

        for (int i = 0; i < embeds.length; i++) {
            if (!(embeds[i] instanceof InternalEmbed internal))
                throw new IllegalArgumentException("Embed was not created using the EmbedBuilder");

            messageEmbeds[i] = internal.embed();
        }

        try (MessageCreateData data = new MessageCreateBuilder().setContent(msg).setEmbeds(messageEmbeds).build()) {
            return new InternalMyMessage(member.getUser().openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage(data))
                    .complete());
        }
    }
}
