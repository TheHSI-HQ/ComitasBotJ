package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum.ForumTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@SuppressWarnings("unused")
public interface ForumPost extends ThreadChannel {
    /**
     * Lists all tags of this thread
     *
     * @return Every tag in this thread
     */
    @NotNull
    @Unmodifiable
    List<ForumTag> getTags();

    /**
     * Add a tag to this thread
     *
     * @param tag The tag to add
     */
    void addTag(@NotNull ForumTag tag);

    /**
     * Remove a tag from this thread
     *
     * @param tag The tag to remove
     */
    void removeTag(@NotNull ForumTag tag);

    /**
     * Does this thread have this tag
     *
     * @param tag The tag to look up
     */
    boolean hasTag(@NotNull ForumTag tag);

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
