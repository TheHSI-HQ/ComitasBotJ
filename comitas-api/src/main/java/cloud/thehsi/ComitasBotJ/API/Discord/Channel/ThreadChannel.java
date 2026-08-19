package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public interface ThreadChannel extends MessageChannel {
    /**
     * Gets the thread's guild
     *
     * @return The thread's guild
     */
    Guild getGuild();

    /**
     * Get the message that started this thread
     *
     * @return The threads initial message
     */
    Message getInitialMessage();

    /**
     * List all member of this thread
     *
     * @return A list of all thread members
     */
    List<Member> getMembers();

    /**
     * Add a member to the thread
     *
     * @param member The member to add
     */
    void addMember(Member member);

    /**
     * Remove a member from the thread
     *
     * @param member The member to remove
     */
    void removeMember(Member member);

    /**
     * Delete this thread
     */
    void delete();

    /**
     * Is this thread private or public
     *
     * @return Is this thread public
     */
    boolean isPublic();

    /**
     * Is this thread opened or closed
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

    /**
     * Get the threads title
     *
     * @return Thread title
     */
    String getTitle();

    /**
     * Set the threads title
     *
     * @param title The new post title
     */
    void setTitle(String title);

    /**
     * Get the threads original poster (OP)
     *
     * @return Thread's Original Poster (if present)
     */
    @Nullable
    Member getOriginalPoster();
}
