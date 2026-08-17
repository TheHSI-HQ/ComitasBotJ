package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;

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
     * Create a forum post
     *
     * @param message The initial message
     * @return The created forum post
     */
    ThreadChannel createPost(String title, Component message);

    /**
     * Create a forum post using message data
     *
     * @param messageData The message data to be sent as the initial message
     * @return The created forum post
     */
    ThreadChannel createPost(String title, MessageData messageData);
}
