package cloud.thehsi.ComitasBotJ.API.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;

@SuppressWarnings("unused")
public interface MyMessage extends Message {
    /**
     * Set the content of this message
     *
     * @param content The new message content
     */
    void setContent(Component content);

    /**
     * Set the content of this message
     *
     * @param content The new message content
     * @param embed The embed to attach to this message
     */
    void setContent(Component content, Embed embed);

    /**
     * RSet the content of this message
     *
     * @param content The new message content
     * @param embeds The embeds to attach to this message
     */
    void setContent(Component content, Embed... embeds);
}
