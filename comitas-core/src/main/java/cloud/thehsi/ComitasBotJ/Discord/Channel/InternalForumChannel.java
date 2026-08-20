package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ForumChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread.TagNameNotUniqueException;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread.TagUsedOnIncorrectChannelException;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread.ThreadTag;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ThreadChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.Thread.InternalThreadTag;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Message.MessageDataParser;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        DebugLogging.action();
        if (channel instanceof GuildChannel guildChannel)
            return new InternalGuild(guildChannel.getGuild());
        return null;
    }

    @Override
    public List<ThreadChannel> getPosts() {
        DebugLogging.action();
        return channel.getThreadChannels().stream()
                .map(e -> (ThreadChannel) new InternalThreadChannel(e))
                .toList();
    }

    @Override
    public List<ThreadTag> getTags() {
        DebugLogging.action();
        return channel.getAvailableTags().stream().map(
                e-> (ThreadTag) new InternalThreadTag(e, channel.getIdLong())
        ).toList();
    }

    @Override
    public @Nullable ThreadTag getTag(String tagName) {
        DebugLogging.action(tagName);
        return getTags().stream()
                .filter(e -> Objects.equals(e.getName(), tagName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public ThreadTag getOrAddTag(String tagName) {
        DebugLogging.action(tagName);
        ThreadTag tag = getTag(tagName);
        if (tag != null)
            return tag;
        try {
            return addTag(tagName);
        } catch (TagNameNotUniqueException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ThreadTag addTag(String tagName) throws TagNameNotUniqueException {
        DebugLogging.action(tagName);
        return Comitas.getUtilityBackend().createTagOnChannel(this, tagName);
    }

    @Override
    public void addTag(ThreadTag tag) throws TagUsedOnIncorrectChannelException {
        DebugLogging.action(tag);
        List<ForumTag> tags = new ArrayList<>(channel.getAvailableTags());
        if (!(tag instanceof InternalThreadTag(ForumTag iTag, long channelId)))
            throw new RuntimeException("ThreadTag was not created by ComitasBotJ");
        if (channelId != channel.getIdLong())
            throw new TagUsedOnIncorrectChannelException("This tag was created for channel " + channelId + " but used on " + channel.getId());

        tags.add(iTag);

        channel.getManager()
                .setAvailableTags(tags)
                .complete();
    }

    @Override
    public void removeTag(ThreadTag tag) throws TagUsedOnIncorrectChannelException {
        DebugLogging.action(tag);
        List<ForumTag> tags = new ArrayList<>(channel.getAvailableTags());
        if (!(tag instanceof InternalThreadTag(ForumTag iTag, long channelId)))
            throw new RuntimeException("ThreadTag was not created by ComitasBotJ");
        if (channelId != channel.getIdLong())
            throw new TagUsedOnIncorrectChannelException("This tag was created for channel " + channelId + " but used on " + channel.getId());

        tags.remove(iTag);

        channel.getManager()
                .setAvailableTags(tags)
                .complete();
    }

    @Override
    public ThreadChannel createPost(String title, Component message) {
        DebugLogging.action(title, message);
        return createPost(title, message.asMessageData());
    }

    @Override
    public ThreadChannel createPost(String title, MessageData messageData) {
        DebugLogging.action(title, messageData);
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
