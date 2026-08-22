package cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface Button extends ActionRowComponent {
    int LABEL_MAX_LENGTH = 80;
    int ID_MAX_LENGTH = 100;
    int URL_MAX_LENGTH = 512;

    static Button primary(String id, String label, Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, label, ButtonStyle.PRIMARY, callback);
    }

    static Button primary(String id, Emoji emoji, Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, emoji, ButtonStyle.PRIMARY, callback);
    }

    static Button secondary(String id, String label, Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, label, ButtonStyle.SECONDARY, callback);
    }

    static Button secondary(String id, Emoji emoji, Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, emoji, ButtonStyle.SECONDARY, callback);
    }

    static Button success(String id, String label, Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, label, ButtonStyle.SUCCESS, callback);
    }

    static Button success(String id, Emoji emoji, Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, emoji, ButtonStyle.SUCCESS, callback);
    }

    static Button danger(String id, String label, Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, label, ButtonStyle.DANGER, callback);
    }

    static Button danger(String id, Emoji emoji, Consumer<ButtonPressedContext> callback) {
        return Comitas.getUtilityBackend().createActionButton(id, emoji, ButtonStyle.DANGER, callback);
    }

    static Button link(String url, String label) {
        return Comitas.getUtilityBackend().createActionButton(url, label, ButtonStyle.LINK, null);
    }

    static Button link(String url, Emoji emoji) {
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
