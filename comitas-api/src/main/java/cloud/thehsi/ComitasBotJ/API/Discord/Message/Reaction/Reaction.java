package cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction;

import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

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
     * @return A list of all reactors
     */
    @NotNull
    @Unmodifiable
    List<Member> getReactors();

    /**
     * Gets the amount of reactions
     *
     * @return The amount of reactions
     */
    int getCount();

    /**
     * Has the bot reacted to this message
     *
     * @return Has the bot reacted to this reaction
     */
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
    void clear();

    /**
     * React with this reaction
     */
    void react();

    /**
     * Removes the bots reaction
     */
    void unreact();

    /**
     * Removes a members reaction
     *
     * @param member TThe member whose reaction is to be removed
     */
    void removeReaction(@NotNull Member member);
}
