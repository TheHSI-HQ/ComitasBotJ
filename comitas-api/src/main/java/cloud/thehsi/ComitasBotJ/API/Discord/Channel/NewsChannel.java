package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a news channel
 */
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

    /**
     * Follow this channel, so crossposted messages also appear in {@code target}
     *
     * @param target The channel to post the messages in, may belong to another guild
     * @throws cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MissingBotPermissionException If the bot lacks {@link cloud.thehsi.ComitasBotJ.API.Discord.Permission#MANAGE_WEBHOOKS} in the target channel
     */
    void follow(@NotNull TextChannel target);
}
