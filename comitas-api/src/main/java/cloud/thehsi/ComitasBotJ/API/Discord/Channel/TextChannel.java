package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;

@SuppressWarnings("unused")
public interface TextChannel extends Channel {
    /**
     * Send a Message in the Channel
     *
     * @param message The message to be sent
     */
    void sendMessage(Component message);

    /**
     * Send a Message in the Channel with an Embed
     *
     * @param message The message to be sent
     * @param embed The embed to attach to this message
     */
    void sendMessage(Component message, Embed embed);

    /**
     * Send a Message in the Channel with multiple Embeds
     *
     * @param message The message to be sent
     * @param embeds The embeds to attach to this message
     */
    void sendMessage(Component message, Embed... embeds);

    /**
     * Gets the channel's guild
     *
     * @return The channel's guild
     */
    Guild getGuild();
}
