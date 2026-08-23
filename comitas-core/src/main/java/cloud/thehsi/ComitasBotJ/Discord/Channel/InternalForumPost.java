package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum.ForumTag;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum.ForumTagUsedOnIncorrectChannelException;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ForumPost;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.Forum.InternalForumTag;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class InternalForumPost extends InternalThreadChannel implements ForumPost {
    @NotNull
    final net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel channel;

    public InternalForumPost(@NotNull net.dv8tion.jda.api.entities.channel.forums.ForumPost channel) {
        super(channel.getThreadChannel());

        this.channel = channel.getThreadChannel();
    }

    @NotNull
    public net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel channel() {
        return channel;
    }

    @Override
    public @NotNull List<ForumTag> getTags() {
        DebugLogging.action();
        return channel.getAppliedTags().stream()
                .map(e -> (ForumTag) new InternalForumTag(e, channel.getIdLong()))
                .toList();
    }

    @Override
    public void addTag(@NotNull ForumTag tag) throws ForumTagUsedOnIncorrectChannelException {
        DebugLogging.action(tag);
        List<net.dv8tion.jda.api.entities.channel.forums.ForumTag> tags = new ArrayList<>(channel.getAppliedTags());
        if (!(tag instanceof InternalForumTag(
                net.dv8tion.jda.api.entities.channel.forums.ForumTag iTag, long channelId
        )))
            throw new RuntimeException("ThreadTag was not created by ComitasBotJ");
        if (channelId != channel.getIdLong())
            throw new ForumTagUsedOnIncorrectChannelException("This tag was created for channel " + channelId + " but used on " + channel.getId());
        tags.add(iTag);

        Set<String> knownTags = new HashSet<>();
        tags = tags.stream()
                .filter(e -> {
                    if (knownTags.contains(e.getName()))
                        return false;
                    knownTags.add(e.getName());
                    return true;
                }).toList();

        channel.getManager()
                .setAppliedTags(tags)
                .complete();
    }

    @Override
    public void removeTag(@NotNull ForumTag tag) throws ForumTagUsedOnIncorrectChannelException {
        DebugLogging.action(tag);
        List<net.dv8tion.jda.api.entities.channel.forums.ForumTag> tags = new ArrayList<>(channel.getAppliedTags());
        if (!(tag instanceof InternalForumTag(
                net.dv8tion.jda.api.entities.channel.forums.ForumTag iTag, long channelId
        )))
            throw new RuntimeException("ThreadTag was not created by ComitasBotJ");
        if (channelId != channel.getIdLong())
            throw new ForumTagUsedOnIncorrectChannelException("This tag was created for channel " + channelId + " but used on " + channel.getId());

        if (tags.stream().map(net.dv8tion.jda.api.entities.channel.forums.ForumTag::getName).anyMatch(iTag.getName()::equals))
            tags.remove(iTag);

        channel.getManager()
                .setAppliedTags(tags)
                .complete();
    }

    @Override
    public boolean hasTag(@NotNull ForumTag tag) throws ForumTagUsedOnIncorrectChannelException {
        DebugLogging.action(tag);
        List<net.dv8tion.jda.api.entities.channel.forums.ForumTag> tags = new ArrayList<>(channel.getAppliedTags());
        if (!(tag instanceof InternalForumTag(
                net.dv8tion.jda.api.entities.channel.forums.ForumTag iTag, long channelId
        )))
            throw new RuntimeException("ThreadTag was not created by ComitasBotJ");
        if (channelId != channel.getIdLong())
            throw new ForumTagUsedOnIncorrectChannelException("This tag was created for channel " + channelId + " but used on " + channel.getId());
        return tags.contains(iTag);
    }


    @Override
    public boolean isPinned() {
        DebugLogging.action();
        return channel.isPinned();
    }

    @Override
    public void setPinned(boolean pinned) {
        DebugLogging.action(pinned);
        channel.getManager().setPinned(pinned).complete();
    }

    @Override
    public @NotNull Message getInitialMessage() {
        DebugLogging.action();
        return Objects.requireNonNull(super.getInitialMessage());
    }
}
