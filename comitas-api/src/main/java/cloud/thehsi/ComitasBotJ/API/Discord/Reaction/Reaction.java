package cloud.thehsi.ComitasBotJ.API.Discord.Reaction;

import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;

@SuppressWarnings("unused")
public interface Reaction {
    /**
     * Gets the reaction's emoji
     *
     * @return The reaction's emoji
     */
    Emoji getEmoji();

    /**
     * Gets the amount of reactions
     *
     * @return The amount of reactions
     */
    int getCount();

    /**
     * Gets the message that this reaction is on
     *
     * @return The reaction's message
     */
    Message getMessage();

    /**
     * Reset this Reaction
     */
    void clear();
}
