package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ForumChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ThreadChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Message.MessageDataParser;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class InternalForumChannel extends InternalChannel implements ForumChannel {
    final net.dv8tion.jda.api.entities.channel.concrete.ForumChannel channel;

    public InternalForumChannel(net.dv8tion.jda.api.entities.channel.concrete.ForumChannel channel) {
        super(channel);

        this.channel = channel;
    }

    public net.dv8tion.jda.api.entities.channel.concrete.ForumChannel channel() {
        return channel;
    }

    @Override
    @Nullable
    public Guild getGuild() {
        if (channel instanceof GuildChannel guildChannel)
            return new InternalGuild(guildChannel.getGuild());
        return null;
    }

    @Override
    public List<ThreadChannel> getPosts() {
        return channel.getThreadChannels().stream()
                .map(e -> (ThreadChannel) new InternalThreadChannel(e))
                .toList();
    }

    @Override
    public ThreadChannel createPost(String title, Component message) {
        return createPost(title, message.asMessageData());
    }

    @Override
    public ThreadChannel createPost(String title, MessageData messageData) {
        AtomicReference<InternalThreadChannel> result = new AtomicReference<>();
        MessageDataParser.parse(messageData, data -> {
            try (MessageCreateData createData = new MessageCreateBuilder()
                    .setContent(data.message())
                    .setEmbeds(data.messageEmbeds())
                    .setFiles(data.fileUploads())
                    .build()) {

                result.set(new InternalThreadChannel(channel.createForumPost(title, createData).complete().getThreadChannel()));
            }
        });

        return result.get();
    }
}
