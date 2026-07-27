package cloud.thehsi.ComitasBotJ.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentParser;
import cloud.thehsi.ComitasBotJ.Discord.Message.Embeds.InternalEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class InternalMyMessage extends InternalMessage implements MyMessage {
    public InternalMyMessage(net.dv8tion.jda.api.entities.Message message) {
        super(message);
    }

    public InternalMyMessage(net.dv8tion.jda.api.entities.Message message, Runnable deletionCallback) {
        super(message, deletionCallback);
    }

    @Override
    public void setContent(Component content) {
        String msg = ComponentParser.parseComponent(content);
        message.editMessage(msg).complete();
        message.editMessageEmbeds().complete();
    }

    @Override
    public void setContent(Component content, Embed embed) {
        String msg = ComponentParser.parseComponent(content);
        message.editMessage(msg).complete();
        if (!(embed instanceof InternalEmbed internal))
            throw new IllegalArgumentException("Embed was not created using the EmbedBuilder");

        MessageEmbed messageEmbed = internal.embed();
        try (MessageCreateData data = new MessageCreateBuilder().setContent(msg).setEmbeds(messageEmbed).build()) {
            this.message.reply(data).complete();
        }
    }

    @Override
    public void setContent(Component content, Embed... embeds) {
        String msg = ComponentParser.parseComponent(content);

        MessageEmbed[] messageEmbeds = new MessageEmbed[embeds.length];

        for (int i = 0; i < embeds.length; i++) {
            if (!(embeds[i] instanceof InternalEmbed internal))
                throw new IllegalArgumentException("Embed was not created using the EmbedBuilder");

            messageEmbeds[i] = internal.embed();
        }

        message.editMessage(msg).complete();
        message.editMessageEmbeds(messageEmbeds).complete();
    }
}