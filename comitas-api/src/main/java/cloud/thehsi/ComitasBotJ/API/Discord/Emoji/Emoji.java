package cloud.thehsi.ComitasBotJ.API.Discord.Emoji;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface Emoji {
    /**
     * Get an emoji by id
     *
     * @return The emoji
     */
    @Nullable
    static Emoji fromId(@NotNull String id) {
        return Comitas.getUtilityBackend().getEmojiFromId(id);
    }

    /**
     * Get an emoji by id
     *
     * @return The emoji
     */
    @Nullable
    static Emoji fromId(long id) {
        return Comitas.getUtilityBackend().getEmojiFromId(id);
    }

    /**
     * Create an emoji from a Unicode character
     *
     * @return The emoji
     */
    @Nullable
    static Emoji fromUnicode(@NotNull String id) {
        return Comitas.getUtilityBackend().getEmojiFromUnicode(id);
    }

    /**
     * Generates the emoji so it can be placed in a message
     *
     * @return The emoji component
     */
    @NotNull
    Component asComponent();

    /**
     * Get the emoji name
     *
     * @return The emoji name
     */
    @NotNull
    String getName();
}
