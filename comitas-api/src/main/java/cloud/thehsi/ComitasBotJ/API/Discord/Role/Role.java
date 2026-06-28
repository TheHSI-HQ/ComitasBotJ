package cloud.thehsi.ComitasBotJ.API.Discord.Role;

import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

@SuppressWarnings("unused")
public interface Role {
    /**
     * Returns the role's Name.
     *
     * @return The role's Name.
     */
    String getName();

    /**
     * Returns the role's ID.
     *
     * @return The role's ID
     */
    Long getId();

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
     * @return The generated Mention-String
     */
    String mention();

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
     * Returns the user's name in there color.
     *
     * @return The user's name with there color
     */
    String getLoggableName();

    /**
     * Generates a list of all role permissions
     *
     * @return The generated list of permissions
     */
    List<Permission> getPermissions();
}
