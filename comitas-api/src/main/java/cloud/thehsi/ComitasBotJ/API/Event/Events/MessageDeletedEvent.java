package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface MessageDeletedEvent extends Event {
    /**
     * Get the id of the deleted message
     *
     * @return The Message Id
     */
    long getId();

    /**
     * Get the Channel the Message was sent in
     *
     * @return The Message Channel
     */
    MessageChannel getChannel();

    /**
     * Get the Guild the Message was sent in
     *
     * @return The Message Guild (Maybe null if dm)
     */
    @Nullable
    Guild getGuild();
}
