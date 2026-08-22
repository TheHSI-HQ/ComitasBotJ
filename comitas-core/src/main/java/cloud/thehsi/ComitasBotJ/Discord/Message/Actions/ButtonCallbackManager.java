package cloud.thehsi.ComitasBotJ.Discord.Message.Actions;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ButtonPressedContext;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ButtonCallbackManager {
    private static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Message.Actions.ButtonCallbackManager");
    private static final Map<String, Consumer<ButtonPressedContext>> callbacks = new HashMap<>();

    public static void registerCallback(InternalButton button) {
        ButtonCallbackManager.callbacks.put(button.button.getCustomId(), button.callback);
    }

    public static void runCallback(ButtonInteractionEvent event) {
        Consumer<ButtonPressedContext> consumer = ButtonCallbackManager.callbacks.get(event.getButton().getCustomId());

        if (consumer == null)
            return;

        consumer.accept(new InternalButtonPressedContext(event));
    }
}
