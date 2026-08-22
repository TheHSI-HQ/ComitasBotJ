package cloud.thehsi.ComitasBotJ.Discord.Message.Actions;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ButtonPressedContext;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class ButtonCallbackManager {
    private static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Message.Actions.ButtonCallbackManager");
    private static final Map<String, ButtomMetadata> metadataMap = new HashMap<>();
    private static final Random rng = new Random(System.currentTimeMillis());

    public static String makeUniqueIdentifier(String id) {
        return rng.nextLong() + "#" + id;
    }

    public static void registerCallback(InternalButton button) {
        ButtonCallbackManager.metadataMap.put(button.internalReferenceId, new ButtomMetadata(
                button.callback,
                button.getId()
        ));
    }

    public static void runCallback(ButtonInteractionEvent event) {
        ButtomMetadata metadata = ButtonCallbackManager.metadataMap.get(event.getButton().getCustomId());

        if (metadata == null)
            return;

        metadata.callback().accept(new InternalButtonPressedContext(event, metadata.id()));
    }

    record ButtomMetadata(Consumer<ButtonPressedContext> callback, String id) {
    }
}
