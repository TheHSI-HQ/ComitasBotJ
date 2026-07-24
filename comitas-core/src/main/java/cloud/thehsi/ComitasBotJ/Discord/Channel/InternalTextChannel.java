package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentParser;
import cloud.thehsi.ComitasBotJ.Discord.Message.Embeds.InternalEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class InternalTextChannel extends InternalChannel implements TextChannel {
    final net.dv8tion.jda.api.entities.channel.concrete.TextChannel textChannel;

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
    public void sendMessage(Component message, Embed embed) {
        String msg = ComponentParser.parseComponent(message);

        if (!(embed instanceof InternalEmbed internal))
            throw new IllegalArgumentException("Embed was not created using the EmbedBuilder");

        MessageEmbed messageEmbed = internal.embed();
        try (MessageCreateData data = new MessageCreateBuilder().setContent(msg).setEmbeds(messageEmbed).build()) {
            textChannel.sendMessage(data).queue();
        }
    }

    @Override
    public void sendMessage(Component message, Embed... embeds) {
        String msg = ComponentParser.parseComponent(message);

        MessageEmbed[] messageEmbeds = new MessageEmbed[embeds.length];

        for (int i = 0; i < embeds.length; i++) {
            if (!(embeds[i] instanceof InternalEmbed internal))
                throw new IllegalArgumentException("Embed was not created using the EmbedBuilder");

            messageEmbeds[i] = internal.embed();
        }

        try (MessageCreateData data = new MessageCreateBuilder().setContent(msg).setEmbeds(messageEmbeds).build()) {
            textChannel.sendMessage(data).queue();
        }
    }

    @Override
    public Guild getGuild() {
        return new InternalGuild(textChannel.getGuild());
    }
}
