package cloud.thehsi.ComitasBotJ.API.Discord.Guild;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public interface Guild {
    /**
     * Returns the guild's Name.
     *
     * @return The guild's Name.
     */
    String getName();

    /**
     * Returns the guild's ID.
     *
     * @return The guild's ID
     */
    Long getId();

    /**
     * Returns the Default Channel of the guild.
     *
     * @return The default channel of this guild.
     */
    MessageChannel getDefaultChannel();

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
    Member getMember(User user);

    /**
     * Returns a list of Invites of the guild.
     *
     * @return An invitation list of this guild.
     */
    List<Invite> getInvites();

    /**
     * Returns a list of users who are banned in this guild.
     *
     * @return A list of banned users in this guild.
     */
    List<Ban> getBans();

    /**
     * Returns a list of Members of the guild.
     *
     * @return A member list of this guild.
     */
    List<Member> getMembers();

    /**
     * Returns a list of Roles of the guild.
     *
     * @return A role list of this guild.
     */
    List<Role> getRoles();

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
    List<Channel> getChannels();

    /**
     * Kick this member
     *
     * @param member The member to be kicked
     */
    void kick(Member member);

    /**
     * Kick this member
     *
     * @param member The member to be kicked
     * @param reason The kick reason
     */
    void kick(Member member, String reason);

    /**
     * Ban a member
     *
     * @param member The member to be banned
     */
    Ban ban(Member member);

    /**
     * Ban a member
     *
     * @param member The member to be banned
     * @param reason The ban reason
     */
    Ban ban(Member member, String reason);

    /**
     * Ban and delete the last {@code deletionPeriodHours} hours of message from a member
     *
     * @param member              The member to be banned
     * @param deletionPeriodHours The amount of hours of messages to delete alongside the ban
     */
    Ban ban(Member member, int deletionPeriodHours);

    /**
     * Ban and delete the last {@code deletionPeriodHours} hours of message from a member
     *
     * @param member              The member to be banned
     * @param reason              The ban reason
     * @param deletionPeriodHours The amount of hours of messages to delete alongside the ban
     */
    Ban ban(Member member, String reason, int deletionPeriodHours);

    /**
     * Unban a user
     *
     * @param user The user to be unbanned
     */
    void unban(User user);

    /**
     * Is the bot a member of this guild
     *
     * @return Is the bot a member of this guild
     */
    boolean amIMember();
}
