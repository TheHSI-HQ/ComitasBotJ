package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageHistory;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMessage;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMessageHistory;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMyMessage;
import cloud.thehsi.ComitasBotJ.Discord.Message.MessageDataParser;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.Nullable;

public class InternalMessageChannel extends InternalChannel implements MessageChannel {
    final net.dv8tion.jda.api.entities.channel.middleman.MessageChannel channel;

    public InternalMessageChannel(net.dv8tion.jda.api.entities.channel.middleman.MessageChannel channel) {
        super(channel);

        this.channel = channel;
    }

    public net.dv8tion.jda.api.entities.channel.middleman.MessageChannel channel() {
        return channel;
    }

    @Override
    public MyMessage sendMessage(Component message) {
        DebugLogging.action(message);
        return sendMessage(message.asMessageData());
    }

    @Override
    public MyMessage sendMessage(MessageData messageData) {
        DebugLogging.action(messageData);
        return MessageDataParser.send(messageData, data -> new InternalMyMessage(
                this.channel.sendMessage(data).complete()
        ));
    }

    @Override
    public MessageHistory getMessageHistory() {
        DebugLogging.action();
        return new InternalMessageHistory(channel.getHistory());
    }

    @Override
    public @Nullable Message getMessageById(long id) {
        DebugLogging.action(id);
        net.dv8tion.jda.api.entities.Message message = channel.retrieveMessageById(id).complete();
        if (message == null)
            return null;
        return new InternalMessage(message);
    }

    @Override
    public @Nullable Message getMessageById(String id) {
        DebugLogging.action(id);
        net.dv8tion.jda.api.entities.Message message = channel.retrieveMessageById(id).complete();
        if (message == null)
            return null;
        return new InternalMessage(message);
    }

    @Override
    @Nullable
    public Guild getGuild() {
        DebugLogging.action();
        if (channel instanceof GuildChannel guildChannel)
            return new InternalGuild(guildChannel.getGuild());
        return null;
    }
}
