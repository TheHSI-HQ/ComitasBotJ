package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface MessageDeletedEvent extends Event {
    /**
     * Get the id of the deleted message
     *
     * @return The Message id
     */
    long getId();

    /**
     * Get the Channel the Message was sent in
     *
     * @return The Message Channel
     */
    @NotNull
    MessageChannel getChannel();

    /**
     * Get the Guild the Message was sent in
     *
     * @return The Message Guild (Maybe null if dm)
     */
    @Nullable
    Guild getGuild();
}
