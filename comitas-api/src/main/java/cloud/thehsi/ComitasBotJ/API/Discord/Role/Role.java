package cloud.thehsi.ComitasBotJ.API.Discord.Role;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.awt.*;
import java.util.List;

/**
 * Represents a role
 */
@SuppressWarnings("unused")
public interface Role {
    /**
     * Returns the role's Name.
     *
     * @return The role's Name.
     */
    @NotNull
    String getName();

    /**
     * Returns the role's ID.
     *
     * @return The role's ID
     */
    long getId();

    /**
     * Get the position of the role
     *
     * @return The position of the role
     */
    int getPosition();

    /**
     * Generates a Mention-String ({@code <@&ROLEID>}).
     * <p>
     * Putting this String in any Discord Message, will mention this Role.
     *
     * @return The generated Mention-Component
     */
    @NotNull
    Component mention();

    /**
     * Returns the role primary color.
     *
     * @return The role primary color
     */
    @Nullable
    Color getPrimaryColor();

    /**
     * Returns the role secondary color.
     *
     * @return The role secondary color
     */
    @Nullable
    Color getSecondaryColor();

    /**
     * Returns the role tertiary color.
     *
     * @return The role tertiary color
     */
    @Nullable
    Color getTertiaryColor();

    /**
     * Returns the role's name in its color in console ascii codes.
     * <p>
     * This is meant for printing the role to the console whilst preserving
     * the role's colors as it would be shown in discord.
     *
     * @return The role's name with its color for console logging
     */
    @NotNull
    String getConsoleLoggableName();

    /**
     * Generates a list of all role permissions
     *
     * @return The generated list of permissions
     */
    @NotNull
    @Unmodifiable
    List<Permission> getPermissions();
}
