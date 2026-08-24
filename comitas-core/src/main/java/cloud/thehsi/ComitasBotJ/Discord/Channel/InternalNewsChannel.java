package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.NewsChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MessageNotFoundException;
import cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.NewsChannelMessageAlreadyCrossposted;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;

public class InternalNewsChannel extends InternalMessageChannel implements NewsChannel {
    @NotNull
    final net.dv8tion.jda.api.entities.channel.concrete.NewsChannel channel;

    public InternalNewsChannel(@NotNull net.dv8tion.jda.api.entities.channel.concrete.NewsChannel channel) {
        super(channel);

        this.channel = channel;
    }

    @NotNull
    public net.dv8tion.jda.api.entities.channel.concrete.NewsChannel channel() {
        return channel;
    }

    @Override
    public @NotNull Guild getGuild() {
        DebugLogging.action();
        return new InternalGuild(channel.getGuild());
    }

    @Override
    public void crosspostMessage(@NotNull Message message) {
        try {
            channel.crosspostMessageById(message.getId()).complete();
        } catch (ErrorResponseException e) {
            if (e.getErrorResponse() == ErrorResponse.ALREADY_CROSSPOSTED)
                throw new NewsChannelMessageAlreadyCrossposted("Message has already been crossposted");
            if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE)
                throw new MessageNotFoundException("Message was not posted in this channel");
            throw e;
        }
    }
}