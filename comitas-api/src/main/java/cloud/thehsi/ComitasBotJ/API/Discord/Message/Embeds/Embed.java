package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;

public interface Embed {
    /**
     * Converts the embed into {@link MessageData}
     */
    MessageData asMessageData();
}
