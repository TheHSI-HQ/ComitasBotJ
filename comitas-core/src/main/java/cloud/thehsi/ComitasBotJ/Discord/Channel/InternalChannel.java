package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ChannelType;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;

public class InternalChannel implements Channel {
    public final net.dv8tion.jda.api.entities.channel.Channel channel;
    ChannelType channelType;

    public InternalChannel(net.dv8tion.jda.api.entities.channel.Channel channel) {
        this.channel = channel;
        this.channelType = ChannelType.fromId(channel.getType().name());
    }

    @Override
    public String getName() {
        DebugLogging.action();
        return channel.getName();
    }

    @Override
    public ChannelType getType() {
        return channelType;
    }

    @Override
    public Long getId() {
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
    public Component mention() {
        DebugLogging.action();
        return Component.raw(channel.getAsMention());
    }
}
