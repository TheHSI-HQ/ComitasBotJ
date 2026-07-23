package cloud.thehsi.ComitasBotJ.Discord.User;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentParser;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
    public String mention() {
        return user.getAsMention();
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
    public CompletableFuture<Boolean> kick() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        member.kick().queue(
                success -> future.complete(true),
                error -> future.complete(false)
        );

        return future;
    }

    @Override
    public CompletableFuture<Boolean> kick(String reason) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        member.kick().reason(reason).queue(
                success -> future.complete(true),
                error -> future.complete(false)
        );

        return future;
    }

    @Override
    public CompletableFuture<Boolean> ban() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        member.ban(0, TimeUnit.DAYS).queue(
                success -> future.complete(true),
                error -> future.complete(false)
        );

        return future;
    }

    @Override
    public CompletableFuture<Boolean> ban(String reason) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        member.ban(0, TimeUnit.DAYS).reason(reason).queue(
                success -> future.complete(true),
                error -> future.complete(false)
        );

        return future;
    }

    @Override
    public CompletableFuture<Boolean> ban(int deletionPeriodHours) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        member.ban(deletionPeriodHours, TimeUnit.HOURS).queue(
                success -> future.complete(true),
                error -> future.complete(false)
        );

        return future;
    }

    @Override
    public CompletableFuture<Boolean> ban(String reason, int deletionPeriodHours) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        member.ban(deletionPeriodHours, TimeUnit.HOURS).reason(reason).queue(
                success -> future.complete(true),
                error -> future.complete(false)
        );

        return future;
    }

    @Override
    public boolean sendDirectMessage(Component message) {
        String msg = ComponentParser.parseComponent(message);

        AtomicBoolean s = new AtomicBoolean(false);
        member.getUser().openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(msg))
                .queue(
                        success -> s.set(true),
                        failure -> s.set(false)
                );
        return s.get();
    }
}
