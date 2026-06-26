package cloud.thehsi.ComitasBotJ.Discord.Role;

import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InternalRole implements Role {
    net.dv8tion.jda.api.entities.Role role;

    public InternalRole(net.dv8tion.jda.api.entities.Role role) {
        this.role = role;
    }

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
    public String mention() {
        return role.getAsMention();
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
    public List<Permission> getPermissions() {
        List<Permission> permissions = new ArrayList<>();
        for (net.dv8tion.jda.api.Permission permission : role.getPermissions())
            permissions.add(
                    Permission.fromValue(permission.name())
            );
        return permissions;
    }
}
