package cloud.thehsi.ComitasBotJ.API.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;

import java.util.List;

public interface MessageHistory {
    /**
     * Returns the Message Channel who owns this history.
     *
     * @return The channel this history is for
     */
    MessageChannel getChannel();

    /**
     * Retrieve the past n messages from the history.
     *
     * @param amount Amount of messages to go back
     * @return The retrieved messages
     */
    List<Message> retrieve(int amount);
}
