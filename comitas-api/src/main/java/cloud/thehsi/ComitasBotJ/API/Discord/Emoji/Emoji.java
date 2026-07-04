package cloud.thehsi.ComitasBotJ.API.Discord.Emoji;

@SuppressWarnings("unused")
public interface Emoji {
    /**
     * Generates the emoji so it can be placed in a message
     *
     * @return The emoji string
     */
    String asMessageEmbed();

    /**
     * Get the emoji name
     *
     * @return The emoji name
     */
    String getName();
}
