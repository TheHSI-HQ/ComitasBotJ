package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;

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

    @Override
    public Guild getGuild() {
        return new InternalGuild(textChannel.getGuild());
    }
}
