package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Attributes.IThreadContainer;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum.ForumTag;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum.ForumTagNameNotUniqueException;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum.ForumTagUsedOnIncorrectChannelException;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@SuppressWarnings("unused")
public interface ForumChannel extends Channel, GuildChannel, IThreadContainer {

    /**
     * Lists all registered tags of this channel
     *
     * @return Every registered tag in this channel
     */
    @NotNull
    @Unmodifiable
    List<ForumTag> getTags();

    /**
     * Get a tag to this channels tag list
     *
     * @param tagName The tag to get
     * @return The found Tag
     */
    @Nullable
    ForumTag getTag(@NotNull String tagName);

    /**
     * Get or Create a tag from / to this channels tag list
     *
     * @param tagName The tag to get / add
     * @return The found / created Tag
     */
    @NotNull
    ForumTag getOrAddTag(@NotNull String tagName);

    /**
     * Create a tag to this channels tag list
     *
     * @param tagName The tag to add
     * @return The created Tag
     */
    @NotNull
    ForumTag addTag(@NotNull String tagName) throws ForumTagNameNotUniqueException;

    /**
     * Register a tag to this channels tag list
     *
     * @param tag The tag to add
     */
    void addTag(@NotNull ForumTag tag) throws ForumTagUsedOnIncorrectChannelException;

    /**
     * Unregister a tag from this channels tag list
     *
     * @param tag The tag to remove
     */
    void removeTag(@NotNull ForumTag tag);

    /**
     * Lists all threads
     *
     * @return Every thread in this channel
     */
    @NotNull
    @Unmodifiable
    List<ForumPost> getPosts();

    /**
     * Create a forum post
     *
     * @param title The forum post title
     * @param message The initial message
     * @return The created forum post
     */
    @NotNull ForumPost createForumPost(@NotNull String title, @NotNull Component message);

    /**
     * Create a forum post using message data
     *
     * @param title The forum post title
     * @param messageData The message data to be sent as the initial message
     * @return The created forum post
     */
    @NotNull ForumPost createForumPost(@NotNull String title, @NotNull MessageData messageData);
}
