package cloud.thehsi.ComitasBotJ.Discord.Reaction;

import cloud.thehsi.ComitasBotJ.API.Discord.Reaction.ReactionAction;

public class InternalReactionAction implements ReactionAction {
    // True is increase, False is decrease
    private final boolean direction;
    private final boolean removed;

    public static final InternalReactionAction INCREASED = new InternalReactionAction(true, false);
    public static final InternalReactionAction DECREASED = new InternalReactionAction(false, false);
    public static final InternalReactionAction REMOVED = new InternalReactionAction(false, true);

    private InternalReactionAction(boolean direction, boolean removed) {
        this.direction = direction;
        this.removed = removed;
    }

    @Override
    public boolean isIncrease() {
        return this.direction;
    }

    @Override
    public boolean isDecrease() {
        return !this.direction;
    }

    @Override
    public boolean isRemoved() {
        return this.removed;
    }
}
