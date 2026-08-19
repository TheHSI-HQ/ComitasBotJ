package cloud.thehsi.ComitasBotJ.API.Bot;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public interface Bot {
    /**
     * Returns the bot's Username.
     *
     * @return The bot's Username.
     */
    String getUserName();

    /**
     * Returns the bot's Display Name.
     *
     * @return The bot's Display Name
     */
    String getDisplayName();

    /**
     * Generates an invitation link for the bot.
     *
     * @return The generated invitation look
     */
    String generateInvitationLink();

    /**
     * Generates an invitation link for the bot.
     *
     * @param permissions A list of permissions to grant the bot
     * @return The generated invitation look
     */
    String generateInvitationLink(Permission... permissions);

    /**
     * Returns the bot's ID.
     *
     * @return The bot's ID
     */
    Long getId();

    /**
     * Get a guild by id
     *
     * @param id The guilds id
     * @return The guild
     */
    @Nullable
    Guild getGuildById(Long id);

    /**
     * Get a guild by id
     *
     * @param id The guilds id
     * @return The guild
     */
    @Nullable
    Guild getGuildById(String id);

    /**
     * Gets a list of Guilds the Bot is a member of.
     *
     * @return A List of Guild.
     */
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
}
