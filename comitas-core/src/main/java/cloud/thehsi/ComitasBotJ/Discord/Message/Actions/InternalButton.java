package cloud.thehsi.ComitasBotJ.Discord.Message.Actions;

import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.Button;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ButtonPressedContext;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ButtonStyle;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Emoji.InternalEmoji;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class InternalButton implements Button, IActionRowComponent {
    public @Nullable String id = null;
    public @Nullable Consumer<ButtonPressedContext> callback = null;
    public @Nullable String internalReferenceId = null;
    public @NotNull
    final net.dv8tion.jda.api.components.buttons.Button button;

    public InternalButton(@NotNull String idOrUrl, @NotNull String label, @NotNull ButtonStyle buttonStyle, @Nullable Consumer<ButtonPressedContext> callback) {
        DebugLogging.action(idOrUrl, label, buttonStyle, callback);

        if (buttonStyle != ButtonStyle.LINK) {
            this.id = idOrUrl;
            this.internalReferenceId = ButtonCallbackManager.makeUniqueIdentifier(this.id);
            this.callback = callback;
            idOrUrl = this.internalReferenceId;
        }

        this.button = net.dv8tion.jda.api.components.buttons.Button.of(
                net.dv8tion.jda.api.components.buttons.ButtonStyle.valueOf(buttonStyle.name()),
                idOrUrl, label
        );
    }

    public InternalButton(@NotNull String idOrUrl, @NotNull Emoji emoji, @NotNull ButtonStyle buttonStyle, @Nullable Consumer<ButtonPressedContext> callback) {
        DebugLogging.action(idOrUrl, emoji, buttonStyle, callback);

        if (!(emoji instanceof InternalEmoji(net.dv8tion.jda.api.entities.emoji.Emoji iEmoji)))
            throw new IllegalArgumentException("Emoji was not created by Comitas");

        if (buttonStyle != ButtonStyle.LINK) {
            this.id = idOrUrl;
            this.internalReferenceId = ButtonCallbackManager.makeUniqueIdentifier(this.id);
            this.callback = callback;
            idOrUrl = this.internalReferenceId;
        }

        this.button = net.dv8tion.jda.api.components.buttons.Button.of(
                net.dv8tion.jda.api.components.buttons.ButtonStyle.valueOf(buttonStyle.name()),
                idOrUrl, iEmoji
        );
    }

    @Override
    public @NotNull ActionRowChildComponent getAsActionRowChildComponent() {
        DebugLogging.action();
        return button;
    }

    @Override
    public @Nullable String getId() {
        DebugLogging.action();
        return id;
    }
}
