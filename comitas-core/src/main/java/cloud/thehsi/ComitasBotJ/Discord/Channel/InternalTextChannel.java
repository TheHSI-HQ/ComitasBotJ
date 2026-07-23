package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentParser;

public class InternalTextChannel extends InternalChannel implements TextChannel {
    net.dv8tion.jda.api.entities.channel.concrete.TextChannel textChannel;

    public InternalTextChannel(net.dv8tion.jda.api.entities.channel.concrete.TextChannel textChannel) {
        super(textChannel);

        this.textChannel = textChannel;
    }

    @Override
    public void sendMessage(Component message) {
        String msg = ComponentParser.parseComponent(message);

        textChannel.sendMessage(msg).queue();
    }

    @Override
    public Guild getGuild() {
        return new InternalGuild(textChannel.getGuild());
    }
}
