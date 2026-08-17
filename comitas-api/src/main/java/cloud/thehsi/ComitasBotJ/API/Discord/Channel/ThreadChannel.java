package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;

@SuppressWarnings("unused")
public interface ThreadChannel extends MessageChannel {
    /**
     * Gets the thread's guild
     *
     * @return The thread's guild
     */
    Guild getGuild();

    /**
     * Delete this thread
     */
    void delete();

    /**
     * Is this thread opened ot closed
     *
     * @return Is this thread closed
     */
    boolean isClosed();

    /**
     * Should this thread be open or closed
     *
     * @param closed Should this thread be closed
     */
    void setClosed(boolean closed);

    /**
     * Is this thread locked
     *
     * @return Is this thread locked
     */
    boolean isLocked();

    /**
     * Lock / Unlock this thread
     *
     * @param locked Should this thread be locked
     */
    void setLocked(boolean locked);

    /**
     * Is this thread pinned
     *
     * @return Is this thread pinned
     */
    boolean isPinned();

    /**
     * Set the pin status of this thread
     *
     * @param pinned Should this thread be pinned
     */
    void setPinned(boolean pinned);
}
