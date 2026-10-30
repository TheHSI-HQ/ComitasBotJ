package cloud.thehsi.ComitasBotJ.Discord.Message.Actions;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ButtonPressedContext;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.Main;
import cloud.thehsi.ComitasBotJ.Plugin.InternalPluginManager;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class ButtonCallbackManager {
    private @NotNull
    static final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Message.Actions.ButtonCallbackManager");

    private static final int MAX_CACHED_CALLBACKS = 10_000;

    private @NotNull
    static final Map<String, ButtomMetadata> metadataMap = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, false) {
                @Override
                protected boolean removeEldestEntry(@NotNull Map.Entry<String, ButtomMetadata> eldest) {
                    return size() > MAX_CACHED_CALLBACKS;
                }
            }
    );
    private @NotNull
    static final Random rng = new Random(System.currentTimeMillis());

    @NotNull
    public static String makeUniqueIdentifier(@NotNull String id) {
        return rng.nextLong() + "#" + id;
    }

    public static void registerCallback(@NotNull InternalButton button) {
        if (button.callback == null || button.getId() == null) return;

        ButtonCallbackManager.metadataMap.put(button.internalReferenceId, new ButtomMetadata(
                button.callback,
                button.getId(),
                Comitas.getPluginManager().getPlugin()
        ));
    }

    /**
     * Drops the callbacks of unloaded plugins, they must not run code of an unloaded plugin.
     */
    public static void unregisterAllExcept(@Nullable Plugin keep) {
        synchronized (metadataMap) {
            metadataMap.values().removeIf(metadata -> metadata.owner() != keep);
        }
    }

    public static void runCallback(@NotNull ButtonInteractionEvent event) {
        ButtomMetadata metadata = ButtonCallbackManager.metadataMap.get(event.getButton().getCustomId());

        if (metadata == null)
            return;

        Plugin previous = InternalPluginManager.setContextPlugin(metadata.owner());
        try {
            metadata.callback().accept(new InternalButtonPressedContext(event, metadata.id()));
        } catch (Exception | LinkageError e) {
            Plugin.PluginMetadata owner = Comitas.getPluginManager().lookupPlugin(metadata.owner());
            logger.error("Error in a button callback of plugin {}", owner == null ? "(unloaded)" : owner.name(), e);
        } finally {
            InternalPluginManager.restoreContextPlugin(previous);
        }
    }

    record ButtomMetadata(Consumer<ButtonPressedContext> callback, String id, Plugin owner) {
    }
}
