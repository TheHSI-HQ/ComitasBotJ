package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread.ThreadTag;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public interface ForumChannel extends Channel {
    /**
     * Gets the channel's guild
     *
     * @return The channel's guild
     */
    Guild getGuild();

    /**
     * Lists all forum posts
     *
     * @return Every post in this channel
     */
    List<ThreadChannel> getPosts();

    /**
     * Lists all registered tags of this channel
     *
     * @return Every registered tag in this channel
     */
    List<ThreadTag> getTags();

    /**
     * Get a tag to this channels tag list
     *
     * @param tagName The tag to get
     * @return The found Tag
     */
    @Nullable
    ThreadTag getTag(String tagName);

    /**
     * Get or Create a tag from / to this channels tag list
     *
     * @param tagName The tag to get / add
     * @return The found / created Tag
     */
    ThreadTag getOrAddTag(String tagName);

    /**
     * Create a tag to this channels tag list
     *
     * @param tagName The tag to add
     * @return The created Tag
     */
    ThreadTag addTag(String tagName);

    /**
     * Register a tag to this channels tag list
     *
     * @param tag The tag to add
     */
    void addTag(ThreadTag tag);

    /**
     * Unregister a tag from this channels tag list
     *
     * @param tag The tag to remove
     */
    void removeTag(ThreadTag tag);

    /**
     * Create a forum post
     *
     * @param title The thread title
     * @param message The initial message
     * @return The created forum post
     */
    ThreadChannel createPost(String title, Component message);

    /**
     * Create a forum post using message data
     *
     * @param title The thread title
     * @param messageData The message data to be sent as the initial message
     * @return The created forum post
     */
    ThreadChannel createPost(String title, MessageData messageData);
}
