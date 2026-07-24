package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;

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
    public Component mention() {
        return Component.raw(channel.getAsMention());
    }
}
