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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class InternalForumChannel extends InternalChannel implements ForumChannel {
    @NotNull
    final net.dv8tion.jda.api.entities.channel.concrete.ForumChannel channel;

    public InternalForumChannel(@NotNull net.dv8tion.jda.api.entities.channel.concrete.ForumChannel channel) {
        super(channel);

        this.channel = channel;
    }

    @NotNull
    public net.dv8tion.jda.api.entities.channel.concrete.ForumChannel channel() {
        return channel;
    }

    @Override
    public @NotNull Guild getGuild() {
        DebugLogging.action();
        return new InternalGuild(channel.getGuild());
    }

    @Override
    public @NotNull List<ThreadChannel> getPosts() {
        DebugLogging.action();
        return channel.getThreadChannels().stream()
                .map(e -> (ThreadChannel) new InternalThreadChannel(e))
                .toList();
    }

    @Override
    public @NotNull List<ThreadTag> getTags() {
        DebugLogging.action();
        return channel.getAvailableTags().stream().map(
                e-> (ThreadTag) new InternalThreadTag(e, channel.getIdLong())
        ).toList();
    }

    @Override
    public @Nullable ThreadTag getTag(@NotNull String tagName) {
        DebugLogging.action(tagName);
        return getTags().stream()
                .filter(e -> Objects.equals(e.getName(), tagName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public @NotNull ThreadTag getOrAddTag(@NotNull String tagName) {
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
    public @NotNull ThreadTag addTag(@NotNull String tagName) throws TagNameNotUniqueException {
        DebugLogging.action(tagName);
        return Comitas.getUtilityBackend().createTagOnChannel(this, tagName);
    }

    @Override
    public void addTag(@NotNull ThreadTag tag) throws TagUsedOnIncorrectChannelException {
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
    public void removeTag(@NotNull ThreadTag tag) throws TagUsedOnIncorrectChannelException {
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
    public @NotNull ThreadChannel createPost(@NotNull String title, @NotNull Component message) {
        DebugLogging.action(title, message);
        return createPost(title, message.asMessageData());
    }

    @Override
    public @NotNull ThreadChannel createPost(@NotNull String title, @NotNull MessageData messageData) {
        DebugLogging.action(title, messageData);
        AtomicReference<InternalThreadChannel> result = new AtomicReference<>();
        MessageDataParser.send(messageData, data -> {
            result.set(
                    new InternalThreadChannel(channel.createForumPost(title, data).complete().getThreadChannel())
            );
            return null;
        });

        return result.get();
    }
}
