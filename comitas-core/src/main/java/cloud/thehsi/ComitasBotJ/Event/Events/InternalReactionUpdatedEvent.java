package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.Reaction.ReactionAction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.Events.ReactionUpdatedEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;

public record InternalReactionUpdatedEvent(Member member, Message message,
                                           Reaction reaction,
                                           ReactionAction reactionAction) implements ReactionUpdatedEvent {
    @Override
    public Member member() {
        DebugLogging.action();
        return member;
    }

    @Override
    public Message message() {
        DebugLogging.action();
        return message;
    }

    @Override
    public Reaction reaction() {
        DebugLogging.action();
        return reaction;
    }

    @Override
    public ReactionAction reactionAction() {
        DebugLogging.action();
        return reactionAction;
    }
}
