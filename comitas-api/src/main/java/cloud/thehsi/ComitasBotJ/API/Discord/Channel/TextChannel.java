package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;

@SuppressWarnings("unused")
public interface TextChannel extends Channel {
    /**
     * Send a Message in the Channel
     *
     * @param message The message to be sent
     */
    void sendMessage(Component message);

    /**
     * Gets the channel's guild
     *
     * @return The channel's guild
     */
    Guild getGuild();
}
