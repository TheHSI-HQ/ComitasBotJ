package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread.TagNameNotUniqueException;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread.TagUsedOnIncorrectChannelException;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread.ThreadTag;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@SuppressWarnings("unused")
public interface ForumChannel extends Channel {
    /**
     * Gets the channel's guild
     *
     * @return The channel's guild
     */
    @NotNull Guild getGuild();

    /**
     * Lists all forum posts
     *
     * @return Every post in this channel
     */
    @NotNull
    @Unmodifiable
    List<ThreadChannel> getPosts();

    /**
     * Lists all registered tags of this channel
     *
     * @return Every registered tag in this channel
     */
    @NotNull
    @Unmodifiable
    List<ThreadTag> getTags();

    /**
     * Get a tag to this channels tag list
     *
     * @param tagName The tag to get
     * @return The found Tag
     */
    @Nullable
    ThreadTag getTag(@NotNull String tagName);

    /**
     * Get or Create a tag from / to this channels tag list
     *
     * @param tagName The tag to get / add
     * @return The found / created Tag
     */
    @NotNull
    ThreadTag getOrAddTag(@NotNull String tagName);

    /**
     * Create a tag to this channels tag list
     *
     * @param tagName The tag to add
     * @return The created Tag
     */
    @NotNull
    ThreadTag addTag(@NotNull String tagName) throws TagNameNotUniqueException;

    /**
     * Register a tag to this channels tag list
     *
     * @param tag The tag to add
     */
    void addTag(@NotNull ThreadTag tag) throws TagUsedOnIncorrectChannelException;

    /**
     * Unregister a tag from this channels tag list
     *
     * @param tag The tag to remove
     */
    void removeTag(@NotNull ThreadTag tag);

    /**
     * Create a forum post
     *
     * @param title The thread title
     * @param message The initial message
     * @return The created forum post
     */
    @NotNull
    ThreadChannel createPost(@NotNull String title, @NotNull Component message);

    /**
     * Create a forum post using message data
     *
     * @param title The thread title
     * @param messageData The message data to be sent as the initial message
     * @return The created forum post
     */
    @NotNull
    ThreadChannel createPost(@NotNull String title, @NotNull MessageData messageData);
}
