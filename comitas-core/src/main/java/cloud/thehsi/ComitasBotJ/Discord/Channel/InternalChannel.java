package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ChannelType;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;
import net.dv8tion.jda.api.entities.channel.attribute.IMemberContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InternalChannel implements Channel {
    public @NotNull
    final net.dv8tion.jda.api.entities.channel.Channel channel;
    @NotNull
    final ChannelType channelType;

    public InternalChannel(@NotNull net.dv8tion.jda.api.entities.channel.Channel channel) {
        this.channel = channel;
        this.channelType = ChannelType.fromId(channel.getType().name());
    }

    @Override
    public @NotNull String getName() {
        DebugLogging.action();
        return channel.getName();
    }

    @Override
    public @NotNull ChannelType getType() {
        return channelType;
    }

    @Override
    public long getId() {
        DebugLogging.action();
        return channel.getIdLong();
    }

    @Override
    public boolean isNSFW() {
        DebugLogging.action();
        if (channel instanceof IAgeRestrictedChannel ageRestricted)
            return ageRestricted.isNSFW();
        return false;
    }

    @Override
    public @NotNull Component mention() {
        DebugLogging.action();
        return Component.raw(channel.getAsMention());
    }

    @Override
    public @Nullable List<Member> getMembers() {
        if (!(channel instanceof IMemberContainer iMemberContainer))
            return null;

        return iMemberContainer.getMembers().stream()
                .map(e -> (Member) new InternalMember(e))
                .toList();
    }
}
