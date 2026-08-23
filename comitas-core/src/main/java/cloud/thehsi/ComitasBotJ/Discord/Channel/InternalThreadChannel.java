package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread.TagUsedOnIncorrectChannelException;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread.ThreadTag;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ThreadChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.Thread.InternalThreadTag;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMessage;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InternalThreadChannel extends InternalMessageChannel implements ThreadChannel {
    @NotNull
    final net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel channel;

    public InternalThreadChannel(@NotNull net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel channel) {
        super(channel);

        this.channel = channel;
    }

    @NotNull
    public net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel channel() {
        return channel;
    }

    @Override
    public @NotNull Guild getGuild() {
        DebugLogging.action();
        return new InternalGuild(channel.getGuild());
    }

    @Override
    public @NotNull Message getInitialMessage() {
        DebugLogging.action();
        return new InternalMessage(channel.retrieveStartMessage().complete());
    }

    @Override
    public @NotNull Channel getParent() {
        DebugLogging.action();
        return ChannelTypeResolver.resolve(channel.getParentChannel());
    }

    @Override
    public @NotNull List<ThreadTag> getTags() {
        DebugLogging.action();
        return channel.getAppliedTags().stream()
                .map(e -> (ThreadTag) new InternalThreadTag(e, channel.getIdLong()))
                .toList();
    }

    @Override
    public void addTag(@NotNull ThreadTag tag) throws TagUsedOnIncorrectChannelException {
        DebugLogging.action(tag);
        List<ForumTag> tags = new ArrayList<>(channel.getAppliedTags());
        if (!(tag instanceof InternalThreadTag(ForumTag iTag, long channelId)))
            throw new RuntimeException("ThreadTag was not created by ComitasBotJ");
        if (channelId != channel.getIdLong())
            throw new TagUsedOnIncorrectChannelException("This tag was created for channel " + channelId + " but used on " + channel.getId());
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
    public void removeTag(@NotNull ThreadTag tag) throws TagUsedOnIncorrectChannelException {
        DebugLogging.action(tag);
        List<ForumTag> tags = new ArrayList<>(channel.getAppliedTags());
        if (!(tag instanceof InternalThreadTag(ForumTag iTag, long channelId)))
            throw new RuntimeException("ThreadTag was not created by ComitasBotJ");
        if (channelId != channel.getIdLong())
            throw new TagUsedOnIncorrectChannelException("This tag was created for channel " + channelId + " but used on " + channel.getId());

        if (tags.stream().map(ForumTag::getName).anyMatch(iTag.getName()::equals))
            tags.remove(iTag);

        channel.getManager()
                .setAppliedTags(tags)
                .complete();
    }

    @Override
    public boolean hasTag(@NotNull ThreadTag tag) throws TagUsedOnIncorrectChannelException {
        DebugLogging.action(tag);
        List<ForumTag> tags = new ArrayList<>(channel.getAppliedTags());
        if (!(tag instanceof InternalThreadTag(ForumTag iTag, long channelId)))
            throw new RuntimeException("ThreadTag was not created by ComitasBotJ");
        if (channelId != channel.getIdLong())
            throw new TagUsedOnIncorrectChannelException("This tag was created for channel " + channelId + " but used on " + channel.getId());
        return tags.contains(iTag);
    }

    @Override
    public @NotNull List<Member> getMembers() {
        DebugLogging.action();
        return channel.retrieveThreadMembers().complete().stream()
                .map(e -> (Member) new InternalMember(e.getMember()))
                .toList();
    }

    @Override
    public void addMember(@NotNull Member member) {
        DebugLogging.action(member);
        if (!(member instanceof InternalMember internalMember))
            throw new RuntimeException("Member is not associated with an internal member");
        channel.addThreadMember(internalMember.member).complete();
    }

    @Override
    public void removeMember(@NotNull Member member) {
        DebugLogging.action(member);
        if (!(member instanceof InternalMember internalMember))
            throw new RuntimeException("Member is not associated with an internal member");
        channel.removeThreadMember(internalMember.member).complete();
    }

    @Override
    public void delete() {
        DebugLogging.action();
        channel.delete().complete();
    }

    @Override
    public boolean isPublic() {
        DebugLogging.action();
        return channel.isPublic();
    }

    @Override
    public boolean isClosed() {
        DebugLogging.action();
        return channel.isArchived();
    }

    @Override
    public void setClosed(boolean closed) {
        DebugLogging.action(closed);
        channel.getManager().setArchived(closed).complete();
    }

    @Override
    public boolean isLocked() {
        DebugLogging.action();
        return channel.isLocked();
    }

    @Override
    public void setLocked(boolean locked) {
        DebugLogging.action(locked);
        channel.getManager().setLocked(locked).complete();
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
    public @NotNull String getTitle() {
        DebugLogging.action();
        return channel.getName();
    }

    @Override
    public void setTitle(@NotNull String title) {
        DebugLogging.action(title);
        channel.getManager().setName(title).complete();
    }

    @Override
    @Nullable
    public Member getOriginalPoster() {
        DebugLogging.action();
        net.dv8tion.jda.api.entities.Member owner = channel.getOwner();
        return owner == null ?
            null : new InternalMember(owner);
    }
}
