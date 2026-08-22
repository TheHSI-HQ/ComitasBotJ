package cloud.thehsi.ComitasBotJ.Discord.Message.Actions;

import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.Button;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ButtonPressedContext;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ButtonStyle;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Emoji.InternalEmoji;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class InternalButton implements Button, IActionRowComponent {
    public final String id;
    public final Consumer<ButtonPressedContext> callback;
    public final net.dv8tion.jda.api.components.buttons.Button button;

    public InternalButton(String idOrUrl, String label, ButtonStyle buttonStyle, Consumer<ButtonPressedContext> callback) {
        this.button = switch (buttonStyle) {
            case PRIMARY -> net.dv8tion.jda.api.components.buttons.Button.primary(idOrUrl, label);
            case SECONDARY -> net.dv8tion.jda.api.components.buttons.Button.secondary(idOrUrl, label);
            case SUCCESS -> net.dv8tion.jda.api.components.buttons.Button.success(idOrUrl, label);
            case DANGER -> net.dv8tion.jda.api.components.buttons.Button.danger(idOrUrl, label);
            case LINK -> net.dv8tion.jda.api.components.buttons.Button.link(idOrUrl, label);
        };
        this.id = switch (buttonStyle) {
            case PRIMARY, SECONDARY, DANGER, SUCCESS -> idOrUrl;
            case LINK -> null;
        };
        this.callback = callback;
    }

    public InternalButton(String idOrUrl, Emoji emoji, ButtonStyle buttonStyle, Consumer<ButtonPressedContext> callback) {
        if (!(emoji instanceof InternalEmoji(net.dv8tion.jda.api.entities.emoji.Emoji iEmoji)))
            throw new IllegalArgumentException("Emoji was not created by Comitas");

        this.button = switch (buttonStyle) {
            case PRIMARY -> net.dv8tion.jda.api.components.buttons.Button.primary(idOrUrl, iEmoji);
            case SECONDARY -> net.dv8tion.jda.api.components.buttons.Button.secondary(idOrUrl, iEmoji);
            case SUCCESS -> net.dv8tion.jda.api.components.buttons.Button.success(idOrUrl, iEmoji);
            case DANGER -> net.dv8tion.jda.api.components.buttons.Button.danger(idOrUrl, iEmoji);
            case LINK -> net.dv8tion.jda.api.components.buttons.Button.link(idOrUrl, iEmoji);
        };
        this.id = switch (buttonStyle) {
            case PRIMARY, SECONDARY, DANGER, SUCCESS -> idOrUrl;
            case LINK -> null;
        };
        this.callback = callback;
    }

    @Override
    public ActionRowChildComponent getAsActionRowChildComponent() {
        DebugLogging.action();
        return button;
    }

    @Override
    public @Nullable String getId() {
        DebugLogging.action();
        return id;
    }
}
