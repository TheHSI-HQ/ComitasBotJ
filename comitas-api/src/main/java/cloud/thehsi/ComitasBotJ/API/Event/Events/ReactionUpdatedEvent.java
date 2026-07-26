package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.Reaction.ReactionAction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;

@SuppressWarnings("unused")
public interface ReactionUpdatedEvent extends Event {
    /**
     * Get the Member who Reacted
     *
     * @return The Message Author
     */
    Member member();

    /**
     * Get the Message which Reactions got updated
     *
     * @return The Message
     */
    Message message();

    /**
     * The updated reaction
     *
     * @return The updated reaction
     */
    Reaction reaction();

    /**
     * Did the reaction count get increased or decreased
     *
     * @return The action that happened
     */
    ReactionAction reactionAction();
}
