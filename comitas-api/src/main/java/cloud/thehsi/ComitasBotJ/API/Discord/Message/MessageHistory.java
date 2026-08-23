package cloud.thehsi.ComitasBotJ.API.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface MessageHistory {
    /**
     * Returns the Message Channel who owns this history.
     *
     * @return The channel this history is for
     */
    @NotNull
    MessageChannel getChannel();

    /**
     * Retrieve the past n messages from the history.
     *
     * @param amount Amount of messages to go back
     * @return The retrieved messages
     */
    @NotNull
    @Unmodifiable
    List<Message> retrieve(int amount);
}
