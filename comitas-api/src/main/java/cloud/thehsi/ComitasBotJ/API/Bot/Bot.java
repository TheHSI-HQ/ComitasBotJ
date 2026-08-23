package cloud.thehsi.ComitasBotJ.API.Bot;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.Activity;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Presence.OnlineStatus;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@SuppressWarnings("unused")
public interface Bot {
    /**
     * Returns the bot's Username.
     *
     * @return The bot's Username.
     */
    @NotNull
    String getUserName();

    /**
     * Returns the bot's Display Name.
     *
     * @return The bot's Display Name
     */
    @NotNull
    String getDisplayName();

    /**
     * Generates an invitation link for the bot.
     *
     * @return The generated invitation look
     */
    @NotNull
    String generateInvitationLink();

    /**
     * Generates an invitation link for the bot.
     *
     * @param permissions A list of permissions to grant the bot
     * @return The generated invitation look
     */
    @NotNull
    String generateInvitationLink(@NotNull Permission... permissions);

    /**
     * Returns the bot as a user.
     *
     * @return The bot cast as user
     */
    @NotNull
    User getUser();

    /**
     * Returns the bot's ID.
     *
     * @return The bot's ID
     */
    long getId();

    /**
     * Get a guild by id
     *
     * @param id The guilds id
     * @return The guild
     */
    @Nullable
    Guild getGuildById(@NotNull Long id);

    /**
     * Get a guild by id
     *
     * @param id The guilds id
     * @return The guild
     */
    @Nullable
    Guild getGuildById(@NotNull String id);

    /**
     * Gets a list of Guilds the Bot is a member of.
     *
     * @return A List of Guild.
     */
    @NotNull
    @Unmodifiable
    List<Guild> getGuilds();

    /**
     * Determine if a {@link Member} is this Bot
     *
     * @param member The member to check
     * @return Is the member this bot
     */
    @Contract("null -> false")
    boolean isMe(@Nullable Member member);

    /**
     * Determine if a {@link Member} is not this Bot and not null
     *
     * @param member The member to check
     * @return Is the member not this bot and not null
     */
    @Contract("null -> true")
    boolean isMeOrNull(@Nullable Member member);

    /**
     * Get the bot's activity
     *
     * @return The bot's activity
     */
    @Nullable
    Activity getActivity();

    /**
     * Set the bot's activity
     *
     * @param activity The bot's new activity
     */
    void setActivity(@Nullable Activity activity);

    /**
     * Get the bot's online status
     *
     * @return The bot's status
     */
    @NotNull
    OnlineStatus getOnlineStatus();

    /**
     * Set the bot's online status
     *
     * @param onlineStatus The bot's new online status
     */
    void setOnlineStatus(@NotNull OnlineStatus onlineStatus);
}
