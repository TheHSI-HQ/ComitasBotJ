package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import net.dv8tion.jda.api.entities.channel.attribute.IAgeRestrictedChannel;

public class InternalChannel implements Channel {
    final net.dv8tion.jda.api.entities.channel.Channel channel;

    public InternalChannel(net.dv8tion.jda.api.entities.channel.Channel channel) {
        this.channel = channel;
    }

    @Override
    public String getName() {
        return channel.getName();
    }

    @Override
    public Long getId() {
        return channel.getIdLong();
    }

    @Override
    public boolean isNSFW() {
        if (channel instanceof IAgeRestrictedChannel ageRestricted)
            return ageRestricted.isNSFW();
        return false;
    }

    @Override
    public Component mention() {
        return Component.raw(channel.getAsMention());
    }
}
