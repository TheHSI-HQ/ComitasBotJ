package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Attributes.IThreadContainer;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@SuppressWarnings("unused")
public interface ThreadChannel extends MessageChannel, GuildChannel {
    /**
     * Gets the thread's guild
     *
     * @return The thread's guild
     */
    @NotNull
    Guild getGuild();

    /**
     * Get the message that started this thread
     *
     * @return The threads initial message
     */
    @Nullable
    Message getInitialMessage();

    /**
     * Get the parent channel of this thread
     *
     * @return The threads parent channel
     */
    @NotNull
    IThreadContainer getParent();

    /**
     * List all member of this thread
     *
     * @return A list of all thread members
     */
    @NotNull
    @Unmodifiable
    List<Member> getMembers();

    /**
     * Add a member to the thread
     *
     * @param member The member to add
     */
    void addMember(@NotNull Member member);

    /**
     * Remove a member from the thread
     *
     * @param member The member to remove
     */
    void removeMember(@NotNull Member member);

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
     * Get the threads title
     *
     * @return Thread title
     */
    @NotNull
    String getTitle();

    /**
     * Set the threads title
     *
     * @param title The new post title
     */
    void setTitle(@NotNull String title);

    /**
     * Get the threads original poster (OP)
     *
     * @return Thread's Original Poster (if present)
     */
    @Nullable
    Member getOriginalPoster();
}
