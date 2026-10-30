package cloud.thehsi.ComitasBotJ.API.Discord.User;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.VoiceChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Ban;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.Activity;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.ClientType;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.OnlineStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.awt.*;
import java.util.List;

/**
 * A {@link User} with a {@link Guild} attached
 */
@SuppressWarnings("unused")
public interface Member extends User {
    /**
     * Returns this member as a member.
     *
     * @return The member as a user
     */
    @NotNull
    User getUser();

    /**
     * Returns the member's guild
     *
     * @return The member's guild
     */
    @NotNull
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
    @NotNull
    @Override
    String getLoggableName();

    /**
     * Generates a list of all member permissions
     *
     * @return The generated list of permissions
     */
    @NotNull
    @Unmodifiable
    List<Permission> getPermissions();

    /**
     * Get the members online status
     *
     * @return Member's online status
     */
    @NotNull
    OnlineStatus getOnlineStatus();

    /**
     * Get the members online status for a specified {@link ClientType}
     *
     * @param clientType The client type to fetch the online status for
     * @return Member's online status
     */
    @NotNull
    OnlineStatus getOnlineStatus(@NotNull ClientType clientType);

    /**
     * Get the member's current activities
     *
     * @return Member's current activities
     */
    @NotNull
    @Unmodifiable
    List<Activity> getActivities();

    /**
     * Add a role to this member
     *
     * @param role The role to add
     */
    void addRole(@NotNull Role role);


    /**
     * Remove a role from this member
     *
     * @param role The role to remove
     */
    void removeRole(@NotNull Role role);


    /**
     * Does this memeber have that role
     *
     * @param role The role to query
     * @return Does the member have this role
     */
    boolean hasRole(@NotNull Role role);

    /**
     * Get all roles of this memeber
     *
     * @return A list of roles of this member
     */
    @NotNull
    @Unmodifiable
    List<Role> getRoles();

    /**
     * Kick this member
     */
    void kick();

    /**
     * Kick this member
     *
     * @param reason The kick reason
     */
    void kick(@NotNull String reason);

    /**
     * Ban this member
     *
     * @return The ban
     */
    @NotNull
    Ban ban();

    /**
     * Ban this member
     *
     * @param reason The ban reason
     * @return The ban
     */
    @NotNull
    Ban ban(@NotNull String reason);

    /**
     * Ban and delete the last {@code deletionPeriodHours} hours of message from this member
     *
     * @param deletionPeriodHours The amount of hours of messages to delete alongside the ban
     * @return The ban
     */
    @NotNull
    Ban ban(int deletionPeriodHours);

    /**
     * Ban and delete the last {@code deletionPeriodHours} hours of message from this member
     *
     * @param reason              The ban reason
     * @param deletionPeriodHours The amount of hours of messages to delete alongside the ban
     * @return The ban
     */
    @NotNull
    Ban ban(@NotNull String reason, int deletionPeriodHours);

    /**
     * Overwrite the members display name
     *
     * @param displayName The new display name, or null to reset to default (global)
     */
    void setDisplayName(@Nullable String displayName);

    /**
     * Get the voice or stage channel this member is connected to
     *
     * @return The connected channel, or null if the member isn't in a voice channel
     */
    @Nullable
    VoiceChannel getVoiceChannel();

    /**
     * Move this member into another voice channel of the same guild
     *
     * @param channel The channel to move the member to
     * @throws cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MemberNotInVoiceException     If the member isn't in a voice channel
     * @throws cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MissingBotPermissionException If the bot lacks {@link Permission#MOVE_MEMBERS} or {@link Permission#CONNECT}
     * @throws IllegalArgumentException                                                      If the channel belongs to another guild
     */
    void moveTo(@NotNull VoiceChannel channel);

    /**
     * Disconnect this member from its voice channel
     *
     * @throws cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MemberNotInVoiceException     If the member isn't in a voice channel
     * @throws cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MissingBotPermissionException If the bot lacks {@link Permission#MOVE_MEMBERS}
     */
    void disconnectFromVoice();

    /**
     * Is this member muted by the server (not by themselves)
     *
     * @return If the member is server muted
     */
    boolean isGuildMuted();

    /**
     * Mute or unmute this member for the whole server
     *
     * @param muted If the member should be muted
     * @throws cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MemberNotInVoiceException     If the member isn't in a voice channel
     * @throws cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MissingBotPermissionException If the bot lacks {@link Permission#MUTE_MEMBERS}
     */
    void setGuildMuted(boolean muted);

    /**
     * Is this member deafened by the server (not by themselves)
     *
     * @return If the member is server deafened
     */
    boolean isGuildDeafened();

    /**
     * Deafen or undeafen this member for the whole server
     *
     * @param deafened If the member should be deafened
     * @throws cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MemberNotInVoiceException     If the member isn't in a voice channel
     * @throws cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MissingBotPermissionException If the bot lacks {@link Permission#DEAFEN_MEMBERS}
     */
    void setGuildDeafened(boolean deafened);
}
