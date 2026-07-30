package cloud.thehsi.ComitasBotJ.API.Discord.User;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Ban;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

@SuppressWarnings("unused")
public interface Member extends User {
    /**
     * Returns this member as a member.
     *
     * @return The member
     */
    User getUser();

    /**
     * Returns the member's guild
     *
     * @return The member's guild
     */
    Guild getGuild();

    /**
     * Returns the member's primary color.
     *
     * @return The member's primary color
     */
    @Nullable
    Color getPrimaryColor();

    /**
     * Returns the member's secondary color.
     *
     * @return The member's secondary color
     */
    @Nullable
    Color getSecondaryColor();

    /**
     * Returns the member's tertiary color.
     *
     * @return The member's tertiary color
     */
    @Nullable
    Color getTertiaryColor();

    /**
     * Returns the member's name in their color.
     *
     * @return The member's name with their color
     */
    @Override
    String getLoggableName();

    /**
     * Generates a list of all member permissions
     *
     * @return The generated list of permissions
     */
    List<Permission> getPermissions();

    /**
     * Kick this member
     */
    void kick();

    /**
     * Kick this member
     *
     * @param reason The kick reason
     */
    void kick(String reason);

    /**
     * Ban this member
     */
    Ban ban();

    /**
     * Ban this member
     *
     * @param reason The ban reason
     */
    Ban ban(String reason);

    /**
     * Ban and delete the last {@code deletionPeriodHours} hours of message from this member
     *
     * @param deletionPeriodHours The amount of hours of messages to delete alongside the ban
     */
    Ban ban(int deletionPeriodHours);

    /**
     * Ban and delete the last {@code deletionPeriodHours} hours of message from this member
     *
     * @param reason              The ban reason
     * @param deletionPeriodHours The amount of hours of messages to delete alongside the ban
     */
    Ban ban(String reason, int deletionPeriodHours);
}
