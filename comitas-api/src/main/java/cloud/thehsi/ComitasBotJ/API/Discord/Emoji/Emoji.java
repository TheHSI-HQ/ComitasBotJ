package cloud.thehsi.ComitasBotJ.API.Discord.Emoji;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents emoji
 */
@SuppressWarnings("unused")
public interface Emoji {
    /**
     * Get an emoji by id
     *
     * @param id The emoji id
     * @return The emoji
     */
    @Nullable
    static Emoji fromId(@NotNull String id) {
        return Comitas.getUtilityBackend().getEmojiFromId(id);
    }

    /**
     * Get an emoji by id
     *
     * @param id The emoji id
     * @return The emoji
     */
    @Nullable
    static Emoji fromId(long id) {
        return Comitas.getUtilityBackend().getEmojiFromId(id);
    }

    /**
     * Create an emoji from a Unicode character
     *
     * @param unicode The emoji Unicode
     * @return The emoji
     */
    @Nullable
    static Emoji fromUnicode(@NotNull String unicode) {
        return Comitas.getUtilityBackend().getEmojiFromUnicode(unicode);
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
