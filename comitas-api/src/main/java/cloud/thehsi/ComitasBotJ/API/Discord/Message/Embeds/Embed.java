package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a message embed
 */
public interface Embed {
    /**
     * Converts the embed into {@link MessageData}
     *
     * @return The generated message data
     */
    @NotNull
    MessageData asMessageData();
}
