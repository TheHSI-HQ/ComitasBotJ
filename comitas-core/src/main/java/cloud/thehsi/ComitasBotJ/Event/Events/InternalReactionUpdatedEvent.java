package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction.ReactionAction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.EventOrigin;
import cloud.thehsi.ComitasBotJ.API.Event.Events.ReactionUpdatedEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class InternalReactionUpdatedEvent extends InternalUndoableEvent implements ReactionUpdatedEvent {
    private @NotNull
    final Member member;
    private @NotNull
    final Message message;
    private @NotNull
    final Reaction reaction;
    private @NotNull
    final ReactionAction reactionAction;
    private @NotNull
    final EventOrigin eventOrigin;

    public InternalReactionUpdatedEvent(@NotNull Member member, @NotNull Message message,
                                        @NotNull Reaction reaction, @NotNull ReactionAction reactionAction,
                                        @NotNull EventOrigin origin) {
        this.member = member;
        this.message = message;
        this.reaction = reaction;
        this.reactionAction = reactionAction;
        this.eventOrigin = origin;
    }

    @Override
    public @NotNull Member member() {
        DebugLogging.action();
        return member;
    }

    @Override
    public @NotNull Message message() {
        DebugLogging.action();
        return message;
    }

    @Override
    public @NotNull Reaction reaction() {
        DebugLogging.action();
        return reaction;
    }

    @Override
    public @NotNull ReactionAction reactionAction() {
        DebugLogging.action();
        return reactionAction;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
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
    @NotNull
    public String toString() {
        return "InternalReactionUpdatedEvent[" +
                "member=" + member + ", " +
                "message=" + message + ", " +
                "reaction=" + reaction + ", " +
                "reactionAction=" + reactionAction + ']';
    }

    @Override
    public @NotNull EventOrigin getOrigin() {
        return eventOrigin;
    }
}
