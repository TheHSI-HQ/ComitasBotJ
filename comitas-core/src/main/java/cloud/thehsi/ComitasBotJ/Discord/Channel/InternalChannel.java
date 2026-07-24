package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;

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
    public String mention() {
        return channel.getAsMention();
    }
}
