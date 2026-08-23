package cloud.thehsi.ComitasBotJ.Discord.Role;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public record InternalRole(net.dv8tion.jda.api.entities.Role role) implements Role {
    @Override
    public @NotNull String getName() {
        DebugLogging.action();
        return role.getName();
    }

    @Override
    public long getId() {
        DebugLogging.action();
        return role.getIdLong();
    }

    @Override
    public int getPosition() {
        DebugLogging.action();
        return role.getPosition();
    }

    @Override
    public @NotNull Component mention() {
        DebugLogging.action();
        return Component.raw(role.getAsMention());
    }

    @Override
    @Nullable
    public Color getPrimaryColor() {
        DebugLogging.action();
        return role.getColors().getPrimary();
    }

    @Override
    @Nullable
    public Color getSecondaryColor() {
        DebugLogging.action();
        return role.getColors().getSecondary();
    }

    @Override
    @Nullable
    public Color getTertiaryColor() {
        DebugLogging.action();
        return role.getColors().getTertiary();
    }

    @Override
    public @NotNull String getLoggableName() {
        DebugLogging.action();
        Color color = Objects.requireNonNullElse(getPrimaryColor(), new Color(153, 170, 181));

        return ConsoleColor.of(color) + getName() + ConsoleColor.RESET;
    }

    @Override
    public @NotNull List<Permission> getPermissions() {
        DebugLogging.action();
        return role.getPermissions().stream()
                .map(e -> Permission.fromValue(e.getName()))
                .toList();
    }
}
