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
    public boolean isIncrease() {
        return this.direction;
    }

    /**
     * @return Did this {@link ReactionAction} decrease the reaction count?
     */
    public boolean isDecrease() {
        return !this.direction;
    }

    /**
     * @return Did this {@link ReactionAction} remove the reaction?
     */
    public boolean isRemoved() {
        return this.removed;
    }
}
