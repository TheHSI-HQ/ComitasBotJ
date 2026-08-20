package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ThreadChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMessage;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InternalThreadChannel extends InternalMessageChannel implements ThreadChannel {
    final net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel channel;

    public InternalThreadChannel(net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel channel) {
        super(channel);

        this.channel = channel;
    }

    public net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel channel() {
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
    public Message getInitialMessage() {
        DebugLogging.action();
        return new InternalMessage(channel.retrieveStartMessage().complete());
    }

    @Override
    public List<Member> getMembers() {
        DebugLogging.action();
        return channel.retrieveThreadMembers().complete().stream()
                .map(e -> (Member) new InternalMember(e.getMember()))
                .toList();
    }

    @Override
    public void addMember(Member member) {
        DebugLogging.action(member);
        if (!(member instanceof InternalMember internalMember))
            throw new RuntimeException("Member is not associated with an internal member");
        channel.addThreadMember(internalMember.member).complete();
    }

    @Override
    public void removeMember(Member member) {
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
    public String getTitle() {
        DebugLogging.action();
        return channel.getName();
    }

    @Override
    public void setTitle(String title) {
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
