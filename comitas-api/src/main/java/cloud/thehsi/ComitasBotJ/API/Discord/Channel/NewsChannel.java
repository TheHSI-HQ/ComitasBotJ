package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface NewsChannel extends MessageChannel, GuildChannel {
    /**
     * Crosspost a message (publish it to all following channels)
     *
     * @param message The message to crosspost
     * @throws cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MessageNotFoundException             If the message doesn't originate from this channel
     * @throws cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.NewsChannelMessageAlreadyCrossposted If the message has already been crossposted
     */
    void crosspostMessage(@NotNull Message message);
}
