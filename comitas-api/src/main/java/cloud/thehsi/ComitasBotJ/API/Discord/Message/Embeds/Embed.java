package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import org.jetbrains.annotations.NotNull;

public interface Embed {
    /**
     * Converts the embed into {@link MessageData}
     */
    @NotNull
    MessageData asMessageData();
}
