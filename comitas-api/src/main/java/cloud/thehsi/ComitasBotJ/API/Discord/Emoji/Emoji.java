package cloud.thehsi.ComitasBotJ.API.Discord.Emoji;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;

@SuppressWarnings("unused")
public interface Emoji {
    /**
     * Generates the emoji so it can be placed in a message
     *
     * @return The emoji component
     */
    Component asMessageEmbed();

    /**
     * Get the emoji name
     *
     * @return The emoji name
     */
    String getName();
}
