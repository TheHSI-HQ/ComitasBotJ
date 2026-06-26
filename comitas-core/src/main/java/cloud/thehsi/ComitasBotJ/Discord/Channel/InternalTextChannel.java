package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;

public class InternalTextChannel extends InternalChannel implements TextChannel {
    net.dv8tion.jda.api.entities.channel.concrete.TextChannel textChannel;

    public InternalTextChannel(net.dv8tion.jda.api.entities.channel.concrete.TextChannel textChannel) {
        super(textChannel);

        this.textChannel = textChannel;
    }

    @Override
    public void sendMessage(String message) {
        textChannel.sendMessage(message).queue();
    }
}
