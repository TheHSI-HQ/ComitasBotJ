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

    /**
     * Is this channel opened ot closed
     *
     * @return Is this channel closed
     */
    boolean isClosed();

    /**
     * Should this channel be open or closed
     *
     * @param closed Should this channel be closed
     */
    void setClosed(boolean closed);

    /**
     * Is this channel pinned
     *
     * @return Is this channel pinned
     */
    boolean isPinned();

    /**
     * Set the pin status of this thread
     *
     * @param pinned Should this channel be pinned
     */
    void setPinned(boolean pinned);
}
