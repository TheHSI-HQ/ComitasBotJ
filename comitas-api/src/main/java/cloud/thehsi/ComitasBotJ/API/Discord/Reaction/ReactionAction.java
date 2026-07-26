package cloud.thehsi.ComitasBotJ.API.Discord.Reaction;

@SuppressWarnings({"unused"})
public interface ReactionAction {
    boolean isIncrease();

    boolean isDecrease();

    boolean isRemoved();
}
