package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;

@SuppressWarnings("unused")
public interface TextChannel extends Channel {
    /**
     * Send a Message in the Channel
     *
     * @param message The message to be sent
     */
    void sendMessage(String message);

    /**
     * Gets the channel's guild
     *
     * @return The channel's guild
     */
    Guild getGuild();
}
