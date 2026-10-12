package cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * An Action Row Button Component
 */
@SuppressWarnings("unused")
public interface Button extends ActionRowComponent {
    /**
     * A labels max length
     */
    int LABEL_MAX_LENGTH = 80;
    /**
     * An ids max length
     */
    int ID_MAX_LENGTH = 100;
    /**
     * A urls max length
     */
    int URL_MAX_LENGTH = 512;

    /**
     * Create a primary {@link Button} with a label
     *
     * @param id       The button id
     * @param label    The button label
     * @param callback The function to call when the button gets pressed
     * @return The generated button
     */
    static @NotNull Button primary(@NotNull String id, @NotNull String label, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, label, ButtonStyle.PRIMARY, callback);
    }

    /**
     * Create a primary {@link Button} with an emoji
     *
     * @param id The button id
     * @param emoji The button emoji
     * @param callback The function to call when the button gets pressed
     * @return The generated button
     */
    static @NotNull Button primary(@NotNull String id, @NotNull Emoji emoji, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, emoji, ButtonStyle.PRIMARY, callback);
    }

    /**
     * Create a secondary {@link Button} with a label
     *
     * @param id The button id
     * @param label The button label
     * @param callback The function to call when the button gets pressed
     * @return The generated button
     */
    static @NotNull Button secondary(@NotNull String id, @NotNull String label, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, label, ButtonStyle.SECONDARY, callback);
    }

    /**
     * Create a secondary {@link Button} with an emoji
     *
     * @param id The button id
     * @param emoji The button emoji
     * @param callback The function to call when the button gets pressed
     * @return The generated button
     */
    static @NotNull Button secondary(@NotNull String id, @NotNull Emoji emoji, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, emoji, ButtonStyle.SECONDARY, callback);
    }

    /**
     * Create a success {@link Button} with a label
     *
     * @param id The button id
     * @param label The button label
     * @param callback The function to call when the button gets pressed
     * @return The generated button
     */
    static @NotNull Button success(@NotNull String id, @NotNull String label, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, label, ButtonStyle.SUCCESS, callback);
    }

    /**
     * Create a success {@link Button} with an emoji
     *
     * @param id The button id
     * @param emoji The button emoji
     * @param callback The function to call when the button gets pressed
     * @return The generated button
     */
    static @NotNull Button success(@NotNull String id, @NotNull Emoji emoji, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, emoji, ButtonStyle.SUCCESS, callback);
    }

    /**
     * Create a danger {@link Button} with a label
     *
     * @param id The button id
     * @param label The button label
     * @param callback The function to call when the button gets pressed
     * @return The generated button
     */
    static @NotNull Button danger(@NotNull String id, @NotNull String label, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, label, ButtonStyle.DANGER, callback);
    }

    /**
     * Create a danger {@link Button} with an emoji
     *
     * @param id The button id
     * @param emoji The button emoji
     * @param callback The function to call when the button gets pressed
     * @return The generated button
     */
    static @NotNull Button danger(@NotNull String id, @NotNull Emoji emoji, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, emoji, ButtonStyle.DANGER, callback);
    }

    /**
     * Create a link {@link Button} with a label
     *
     * @param url The button url
     * @param label The button label
     * @return The generated button
     */
    static @NotNull Button link(@NotNull String url, @NotNull String label) {
        return Comitas.getUtilityBackend().createActionButton(url, label, ButtonStyle.LINK, null);
    }

    /**
     * Create an emoji {@link Button} with an emoji
     *
     * @param url The button url
     * @param emoji The button emoji
     * @return The generated button
     */
    static @NotNull Button link(@NotNull String url, @NotNull Emoji emoji) {
        return Comitas.getUtilityBackend().createActionButton(url, emoji, ButtonStyle.LINK, null);
    }

    /**
     * Get the button id if present (should be if not link)
     *
     * @return The button's id
     */
    @Nullable
    String getId();
}
