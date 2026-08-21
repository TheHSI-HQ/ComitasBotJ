package cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction;

public enum ReactionAction {
    INCREASED(true, false),
    DECREASED(false, false),
    REMOVED(false, true);

    // True is increase, False is decrease
    private final boolean direction;
    private final boolean removed;

    ReactionAction(boolean direction, boolean removed) {
        this.direction = direction;
        this.removed = removed;
    }

    /**
     * @return Did this {@link ReactionAction} increase the reaction count?
     */
    boolean isIncrease() {
        return this.direction;
    }

    /**
     * @return Did this {@link ReactionAction} decrease the reaction count?
     */
    boolean isDecrease() {
        return !this.direction;
    }

    /**
     * @return Did this {@link ReactionAction} remove the reaction?
     */
    boolean isRemoved() {
        return this.removed;
    }
}
