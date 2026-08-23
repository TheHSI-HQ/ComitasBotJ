package cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface Button extends ActionRowComponent {
    int LABEL_MAX_LENGTH = 80;
    int ID_MAX_LENGTH = 100;
    int URL_MAX_LENGTH = 512;

    static @NotNull Button primary(@NotNull String id, @NotNull String label, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, label, ButtonStyle.PRIMARY, callback);
    }

    static @NotNull Button primary(@NotNull String id, @NotNull Emoji emoji, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, emoji, ButtonStyle.PRIMARY, callback);
    }

    static @NotNull Button secondary(@NotNull String id, @NotNull String label, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, label, ButtonStyle.SECONDARY, callback);
    }

    static @NotNull Button secondary(@NotNull String id, @NotNull Emoji emoji, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, emoji, ButtonStyle.SECONDARY, callback);
    }

    static @NotNull Button success(@NotNull String id, @NotNull String label, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, label, ButtonStyle.SUCCESS, callback);
    }

    static @NotNull Button success(@NotNull String id, @NotNull Emoji emoji, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, emoji, ButtonStyle.SUCCESS, callback);
    }

    static @NotNull Button danger(@NotNull String id, @NotNull String label, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, label, ButtonStyle.DANGER, callback);
    }

    static @NotNull Button danger(@NotNull String id, @NotNull Emoji emoji, @NotNull Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, emoji, ButtonStyle.DANGER, callback);
    }

    static @NotNull Button link(@NotNull String url, @NotNull String label) {
        return Comitas.getUtilityBackend().createActionButton(url, label, ButtonStyle.LINK, null);
    }

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
