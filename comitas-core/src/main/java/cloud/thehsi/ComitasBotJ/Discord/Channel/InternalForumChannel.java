package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum.ForumTag;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum.ForumTagNameNotUniqueException;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum.ForumTagUsedOnIncorrectChannelException;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ForumChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ForumPost;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ThreadChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.Forum.InternalForumTag;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Message.MessageDataParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

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
    public @NotNull List<ThreadChannel> getThreads() {
        DebugLogging.action();
        return channel.getThreadChannels().stream()
                .map(e -> (ThreadChannel) new InternalThreadChannel(e))
                .toList();
    }

    @Override
    public @NotNull List<ForumTag> getTags() {
        DebugLogging.action();
        return channel.getAvailableTags().stream().map(
                e -> (ForumTag) new InternalForumTag(e, channel.getIdLong())
        ).toList();
    }

    @Override
    public @Nullable ForumTag getTag(@NotNull String tagName) {
        DebugLogging.action(tagName);
        return getTags().stream()
                .filter(e -> Objects.equals(e.getName(), tagName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public @NotNull ForumTag getOrAddTag(@NotNull String tagName) {
        DebugLogging.action(tagName);
        ForumTag tag = getTag(tagName);
        if (tag != null)
            return tag;
        try {
            return addTag(tagName);
        } catch (ForumTagNameNotUniqueException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull ForumTag addTag(@NotNull String tagName) throws ForumTagNameNotUniqueException {
        DebugLogging.action(tagName);
        return Comitas.getUtilityBackend().createTagOnChannel(this, tagName);
    }

    @Override
    public void addTag(@NotNull ForumTag tag) throws ForumTagUsedOnIncorrectChannelException {
        DebugLogging.action(tag);
        List<net.dv8tion.jda.api.entities.channel.forums.ForumTag> tags = new ArrayList<>(channel.getAvailableTags());
        if (!(tag instanceof InternalForumTag(
                net.dv8tion.jda.api.entities.channel.forums.ForumTag iTag, long channelId
        )))
            throw new RuntimeException("ThreadTag was not created by ComitasBotJ");
        if (channelId != channel.getIdLong())
            throw new ForumTagUsedOnIncorrectChannelException("This tag was created for channel " + channelId + " but used on " + channel.getId());

        tags.add(iTag);

        channel.getManager()
                .setAvailableTags(tags)
                .complete();
    }

    @Override
    public void removeTag(@NotNull ForumTag tag) throws ForumTagUsedOnIncorrectChannelException {
        DebugLogging.action(tag);
        List<net.dv8tion.jda.api.entities.channel.forums.ForumTag> tags = new ArrayList<>(channel.getAvailableTags());
        if (!(tag instanceof InternalForumTag(
                net.dv8tion.jda.api.entities.channel.forums.ForumTag iTag, long channelId
        )))
            throw new RuntimeException("ThreadTag was not created by ComitasBotJ");
        if (channelId != channel.getIdLong())
            throw new ForumTagUsedOnIncorrectChannelException("This tag was created for channel " + channelId + " but used on " + channel.getId());

        tags.remove(iTag);

        channel.getManager()
                .setAvailableTags(tags)
                .complete();
    }

    @Override
    public @NotNull @Unmodifiable List<ForumPost> getPosts() {
        return channel.getThreadChannels().stream()
                .filter(e -> e instanceof net.dv8tion.jda.api.entities.channel.forums.ForumPost)
                .map(e -> (ForumPost) new InternalForumPost((net.dv8tion.jda.api.entities.channel.forums.ForumPost) e))
                .toList();
    }


    @Override
    public @NotNull ThreadChannel createThread(@NotNull String title) {
        DebugLogging.action(title);
        return createThread(title, false);
    }

    @Override
    public @NotNull ThreadChannel createThread(@NotNull String title, boolean isPrivate) {
        DebugLogging.action(title, isPrivate);
        return new InternalThreadChannel(channel.createThreadChannel(title, isPrivate).complete());
    }

    @Override
    public @NotNull ForumPost createForumPost(@NotNull String title, @NotNull Component message) {
        DebugLogging.action(title, message);
        return createForumPost(title, message.asMessageData());
    }

    @Override
    public @NotNull ForumPost createForumPost(@NotNull String title, @NotNull MessageData messageData) {
        DebugLogging.action(title, messageData);
        AtomicReference<InternalForumPost> result = new AtomicReference<>();
        MessageDataParser.send(messageData, data -> {
            result.set(
                    new InternalForumPost(channel.createForumPost(title, data).complete())
            );
            return null;
        });

        return result.get();
    }
}
