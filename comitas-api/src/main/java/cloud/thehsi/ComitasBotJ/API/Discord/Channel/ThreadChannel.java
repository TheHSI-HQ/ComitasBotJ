package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;

@SuppressWarnings("unused")
public interface ThreadChannel extends MessageChannel {
    /**
     * Gets the channel's guild
     *
     * @return The channel's guild
     */
    Guild getGuild();

    /**
     * Delete this Post
     */
    void delete();
}
