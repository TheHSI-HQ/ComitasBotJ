package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction.ReactionAction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.Events.ReactionUpdatedEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;

import java.util.Objects;

public final class InternalReactionUpdatedEvent extends InternalUndoableEvent implements ReactionUpdatedEvent {
    private final Member member;
    private final Message message;
    private final Reaction reaction;
    private final ReactionAction reactionAction;

    public InternalReactionUpdatedEvent(Member member, Message message,
                                        Reaction reaction,
                                        ReactionAction reactionAction) {
        this.member = member;
        this.message = message;
        this.reaction = reaction;
        this.reactionAction = reactionAction;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (InternalReactionUpdatedEvent) obj;
        return Objects.equals(this.member, that.member) &&
                Objects.equals(this.message, that.message) &&
                Objects.equals(this.reaction, that.reaction) &&
                Objects.equals(this.reactionAction, that.reactionAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(member, message, reaction, reactionAction);
    }

    @Override
    public String toString() {
        return "InternalReactionUpdatedEvent[" +
                "member=" + member + ", " +
                "message=" + message + ", " +
                "reaction=" + reaction + ", " +
                "reactionAction=" + reactionAction + ']';
    }

}
