package cloud.thehsi.ComitasBotJ.API.Discord.Reaction;

@SuppressWarnings({"unused"})
public interface ReactionAction {
    /**
     * @return Did this {@link ReactionAction} increase the reaction count?
     */
    boolean isIncrease();

    /**
     * @return Did this {@link ReactionAction} decrease the reaction count?
     */
    boolean isDecrease();

    /**
     * @return Did this {@link ReactionAction} remove the reaction?
     */
    boolean isRemoved();
}
