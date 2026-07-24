package cloud.thehsi.ComitasBotJ.Discord.Role;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record InternalRole(net.dv8tion.jda.api.entities.Role role) implements Role {
    @Override
    public String getName() {
        return role.getName();
    }

    @Override
    public Long getId() {
        return role.getIdLong();
    }

    @Override
    public int getPosition() {
        return role.getPosition();
    }

    @Override
    public Component mention() {
        return Component.raw(role.getAsMention());
    }

    @Override
    public Color getPrimaryColor() {
        return role.getColors().getPrimary();
    }

    @Override
    public Color getSecondaryColor() {
        return role.getColors().getSecondary();
    }

    @Override
    public Color getTertiaryColor() {
        return role.getColors().getTertiary();
    }

    @Override
    public String getLoggableName() {
        Color color = Objects.requireNonNullElse(getPrimaryColor(), new Color(153, 170, 181));

        return ConsoleColor.of(color) + getName() + ConsoleColor.RESET;
    }

    @Override
    public List<Permission> getPermissions() {
        List<Permission> permissions = new ArrayList<>();
        for (net.dv8tion.jda.api.Permission permission : role.getPermissions())
            permissions.add(
                    Permission.fromValue(permission.name())
            );
        return permissions;
    }
}
