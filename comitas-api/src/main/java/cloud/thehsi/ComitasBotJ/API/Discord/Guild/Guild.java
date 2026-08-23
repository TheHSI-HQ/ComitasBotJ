package cloud.thehsi.ComitasBotJ.API.Discord.Guild;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@SuppressWarnings("unused")
public interface Guild {
    /**
     * Returns the guild's Name.
     *
     * @return The guild's Name.
     */
    @NotNull
    String getName();

    /**
     * Returns the guild's ID.
     *
     * @return The guild's ID
     */
    long getId();

    /**
     * Get a channel in this guild by id
     *
     * @param id The channels id
     * @return The channel if found
     */
    @Nullable
    Channel getChannelById(long id);

    /**
     * Get a channel in this guild by id
     *
     * @param id The channels id
     * @return The channel if found
     */
    @Nullable
    Channel getChannelById(@NotNull String id);

    /**
     * Returns the Default Channel of the guild.
     *
     * @return The default channel of this guild.
     */
    @Nullable
    TextChannel getDefaultChannel();

    /**
     * Get the guild icon
     *
     * @return The url of the icon if present
     */
    @Nullable
    String getIconUrl();

    /**
     * Get the guild banner
     *
     * @return The url of the banner if present
     */
    @Nullable
    String getBannerUrl();

    /**
     * Get a member from user, if not a member returns null
     *
     * @param user The user to look up
     * @return The member or null
     */
    @Nullable
    Member getMember(@NotNull User user);

    /**
     * Returns a list of Invites of the guild.
     *
     * @return An invitation list of this guild.
     */
    @NotNull
    @Unmodifiable
    List<Invite> getInvites();

    /**
     * Returns a list of users who are banned in this guild.
     *
     * @return A list of banned users in this guild.
     */
    @NotNull
    @Unmodifiable
    List<Ban> getBans();

    /**
     * Returns a list of Members of the guild.
     *
     * @return A member list of this guild.
     */
    @NotNull
    @Unmodifiable
    List<Member> getMembers();

    /**
     * Returns a list of Roles of the guild.
     *
     * @return A role list of this guild.
     */
    @NotNull
    @Unmodifiable
    List<Role> getRoles();

    /**
     * Returns a role based on its id.
     *
     * @return The role if existing.
     */
    @Nullable
    Role getRoleById(long id);

    /**
     * Returns a role based on its id.
     *
     * @return The role if existing.
     */
    @Nullable
    Role getRoleById(@NotNull String id);

    /**
     * Returns the member count of the guild.
     *
     * @return The member count of the guild.
     */
    int getMemberCount();

    /**
     * Get the description of this guild
     *
     * @return This guilds description
     */
    @Nullable
    String getDescription();

    /**
     * Is this guild a community?
     *
     * @return Is this guild a community?
     */
    boolean isCommunity();

    /**
     * Returns a list of Channels of the guild.
     *
     * @return A channel list of this guild.
     */
    @NotNull
    @Unmodifiable
    List<Channel> getChannels();

    /**
     * Kick this member
     *
     * @param member The member to be kicked
     */
    void kick(@NotNull Member member);

    /**
     * Kick this member
     *
     * @param member The member to be kicked
     * @param reason The kick reason
     */
    void kick(@NotNull Member member, @Nullable String reason);

    /**
     * Ban a member
     *
     * @param member The member to be banned
     */
    @NotNull
    Ban ban(@NotNull Member member);

    /**
     * Ban a member
     *
     * @param member The member to be banned
     * @param reason The ban reason
     */
    @NotNull
    Ban ban(@NotNull Member member, @Nullable String reason);

    /**
     * Ban and delete the last {@code deletionPeriodHours} hours of message from a member
     *
     * @param member              The member to be banned
     * @param deletionPeriodHours The amount of hours of messages to delete alongside the ban
     */
    @NotNull
    Ban ban(@NotNull Member member, @Nullable Integer deletionPeriodHours);

    /**
     * Ban and delete the last {@code deletionPeriodHours} hours of message from a member
     *
     * @param member              The member to be banned
     * @param reason              The ban reason
     * @param deletionPeriodHours The amount of hours of messages to delete alongside the ban
     */
    @NotNull
    Ban ban(@NotNull Member member, @Nullable String reason, @Nullable Integer deletionPeriodHours);

    /**
     * Unban a user
     *
     * @param user The user to be unbanned
     */
    void unban(@NotNull User user);

    /**
     * Is the bot a member of this guild
     *
     * @return Is the bot a member of this guild
     */
    boolean amIMember();
}
