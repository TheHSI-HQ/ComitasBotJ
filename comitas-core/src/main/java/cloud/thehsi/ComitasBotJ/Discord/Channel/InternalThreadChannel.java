package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ThreadChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.Nullable;

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
        if (channel instanceof GuildChannel guildChannel)
            return new InternalGuild(guildChannel.getGuild());
        return null;
    }

    @Override
    public void delete() {
        channel.delete().complete();
    }

    @Override
    public boolean isClosed() {
        return channel.isArchived();
    }

    @Override
    public void setClosed(boolean closed) {
        channel.getManager().setArchived(closed).complete();
    }

    @Override
    public boolean isLocked() {
        return channel.isLocked();
    }

    @Override
    public void setLocked(boolean locked) {
        channel.getManager().setLocked(locked).complete();
    }

    @Override
    public boolean isPinned() {
        return channel.isPinned();
    }

    @Override
    public void setPinned(boolean pinned) {
        channel.getManager().setPinned(pinned).complete();
    }
}
