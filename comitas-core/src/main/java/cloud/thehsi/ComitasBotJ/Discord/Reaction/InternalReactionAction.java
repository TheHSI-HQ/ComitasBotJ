package cloud.thehsi.ComitasBotJ.Discord.Reaction;

import cloud.thehsi.ComitasBotJ.API.Discord.Reaction.ReactionAction;
import cloud.thehsi.ComitasBotJ.DebugLogging;

@SuppressWarnings("ClassCanBeRecord")
public class InternalReactionAction implements ReactionAction {
    public static final InternalReactionAction INCREASED = new InternalReactionAction(true, false);
    public static final InternalReactionAction DECREASED = new InternalReactionAction(false, false);
    public static final InternalReactionAction REMOVED = new InternalReactionAction(false, true);
    // True is increase, False is decrease
    private final boolean direction;
    private final boolean removed;

    private InternalReactionAction(boolean direction, boolean removed) {
        this.direction = direction;
        this.removed = removed;
    }

    @Override
    public boolean isIncrease() {
        DebugLogging.action();
        return this.direction;
    }

    @Override
    public boolean isDecrease() {
        DebugLogging.action();
        return !this.direction;
    }

    @Override
    public boolean isRemoved() {
        DebugLogging.action();
        return this.removed;
    }
}
