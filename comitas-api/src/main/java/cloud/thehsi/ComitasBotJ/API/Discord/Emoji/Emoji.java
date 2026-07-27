package cloud.thehsi.ComitasBotJ.API.Discord.Emoji;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Get an emoji by id
     *
     * @return The emoji
     */
    @Nullable
    static Emoji from(String id) {
        return Comitas.getUtilityBackend().getEmojiFromId(id);
    }

    /**
     * Get an emoji by id
     *
     * @return The emoji
     */
    @Nullable
    static Emoji from(long id) {
        return Comitas.getUtilityBackend().getEmojiFromId(id);
    }
}
