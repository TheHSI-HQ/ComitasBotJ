package cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction;

/**
 * A {@link ReactionAction} is used for the {@link cloud.thehsi.ComitasBotJ.API.Event.Events.ReactionUpdatedEvent}
 * to denote rather a reaction was either added, removed, or completely dissapeared
 */
public enum ReactionAction {
    /**
     * The reaction count increased
     */
    INCREASED(true, false),
    /**
     * The reaction count decreased
     */
    DECREASED(false, false),
    /**
     * The reaction was removed
     */
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
