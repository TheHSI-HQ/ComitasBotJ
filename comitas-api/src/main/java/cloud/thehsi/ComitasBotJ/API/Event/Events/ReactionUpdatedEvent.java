package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction.ReactionAction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface ReactionUpdatedEvent extends Event, UndoableEvent {
    /**
     * Get the Member who Reacted
     *
     * @return The Message Author
     */
    @NotNull
    Member member();

    /**
     * Get the Message which Reactions got updated
     *
     * @return The Message
     */
    @NotNull
    Message message();

    /**
     * The updated reaction
     *
     * @return The updated reaction
     */
    @NotNull
    Reaction reaction();

    /**
     * Did the reaction count get increased or decreased
     *
     * @return The action that happened
     */
    @NotNull
    ReactionAction reactionAction();
}
