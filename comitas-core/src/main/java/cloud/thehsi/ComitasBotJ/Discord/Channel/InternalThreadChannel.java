package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Attributes.IThreadContainer;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ThreadChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMessage;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    public @Nullable Message getInitialMessage() {
        DebugLogging.action();
        net.dv8tion.jda.api.entities.Message message = channel.retrieveStartMessage().complete();
        if (message == null)
            return null;
        return new InternalMessage(message);
    }

    @Override
    public @NotNull IThreadContainer getParent() {
        DebugLogging.action();
        Channel resolvedChannel = ChannelTypeResolver.resolve(channel.getParentChannel());
        if (resolvedChannel instanceof IThreadContainer iThreadContainer)
            return iThreadContainer;
        throw new RuntimeException("The parent channel isn't a IThreadContainer.");
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
