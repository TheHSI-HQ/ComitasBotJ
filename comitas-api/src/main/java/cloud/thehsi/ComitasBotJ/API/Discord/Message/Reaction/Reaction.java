package cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction;

import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.RequiresBotPermission;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Represents a reaction
 */
@SuppressWarnings("unused")
public interface Reaction {
    /**
     * Gets the reaction's emoji
     *
     * @return The reaction's emoji
     */
    @NotNull
    Emoji getEmoji();

    /**
     * Fetches a list of everyone who reacted to this message
     *
     * @return A list of all users
     */
    @RequiresBotPermission(Permission.READ_MESSAGE_HISTORY)
    @NotNull
    @Unmodifiable
    List<User> getReactors();

    /**
     * Fetches a list of everyone who reacted to this message
     *
     * @return A list of all reactors as members, null if in a dm
     */
    @RequiresBotPermission(Permission.READ_MESSAGE_HISTORY)
    @Nullable
    @Unmodifiable
    List<Member> getReactorMembers();

    /**
     * Gets the amount of reactions
     *
     * @return The amount of reactions
     */
    @RequiresBotPermission(conditional = Permission.READ_MESSAGE_HISTORY)
    int getCount();

    /**
     * Has the bot reacted to this message
     *
     * @return Has the bot reacted to this reaction
     */
    @RequiresBotPermission(Permission.READ_MESSAGE_HISTORY)
    boolean haveIReacted();

    /**
     * Does this reaction still exist on the message
     *
     * @return Reaction existence
     */
    boolean exists();

    /**
     * Gets the message that this reaction is on
     *
     * @return The reaction's message
     */
    @NotNull
    Message getMessage();

    /**
     * Reset this Reaction
     */
    @RequiresBotPermission(Permission.MANAGE_MESSAGES)
    void clear();

    /**
     * React with this reaction
     */
    @RequiresBotPermission(value = {Permission.ADD_REACTIONS, Permission.READ_MESSAGE_HISTORY}, conditional = Permission.USE_EXTERNAL_EMOJIS)
    void react();

    /**
     * Removes the bots reaction
     */
    @RequiresBotPermission(Permission.READ_MESSAGE_HISTORY)
    void unreact();

    /**
     * Removes a members reaction
     *
     * @param member TThe member whose reaction is to be removed
     */
    @RequiresBotPermission(value = Permission.READ_MESSAGE_HISTORY, conditional = Permission.MANAGE_MESSAGES)
    void removeReaction(@NotNull Member member);
}
