package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;

@SuppressWarnings("unused")
public interface MessageChannel extends Channel {
    /**
     * Send a Message in the Channel
     *
     * @param message The message to be sent
     * @return The message that was sent
     */
    MyMessage sendMessage(Component message);

    /**
     * Send a Message in the Channel with an Embed
     *
     * @param message The message to be sent
     * @param embed   The embed to attach to this message
     * @return The message that was sent
     */
    MyMessage sendMessage(Component message, Embed embed);

    /**
     * Send a Message in the Channel with multiple Embeds
     *
     * @param message The message to be sent
     * @param embeds  The embeds to attach to this message
     * @return The message that was sent
     */
    MyMessage sendMessage(Component message, Embed... embeds);

    /**
     * Gets the channel's guild
     *
     * @return The channel's guild
     */
    Guild getGuild();
}
