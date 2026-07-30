package cloud.thehsi.ComitasBotJ.Discord.User;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentParser;
import cloud.thehsi.ComitasBotJ.Discord.Message.Embeds.InternalEmbed;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMyMessage;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;

public class InternalUser implements User {
    public final net.dv8tion.jda.api.entities.User user;

    public InternalUser(net.dv8tion.jda.api.entities.User user) {
        this.user = user;
    }

    @Override
    public String getUserName() {
        return user.getName();
    }

    @Override
    public String getDisplayName() {
        return user.getEffectiveName();
    }

    @Override
    public Long getId() {
        return user.getIdLong();
    }

    @Override
    public boolean isBot() {
        return user.isBot();
    }

    @Override
    public boolean isMe() {
        return user.getIdLong() == user.getJDA().getSelfUser().getIdLong();
    }

    @Override
    public String getLoggableName() {
        return getDisplayName();
    }

    @Override
    public Component mention() {
        return Component.raw(user.getAsMention());
    }

    @Override
    public MyMessage sendDirectMessage(Component message) {
        String msg = ComponentParser.parseComponent(message);

        return new InternalMyMessage(user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(msg))
                .complete());
    }

    @Override
    public MyMessage sendDirectMessage(Component message, Embed embed) {
        String msg = ComponentParser.parseComponent(message);

        if (!(embed instanceof InternalEmbed internal))
            throw new IllegalArgumentException("Embed was not created using the EmbedBuilder");

        MessageEmbed messageEmbed = internal.embed();

        try (MessageCreateData data = new MessageCreateBuilder().setContent(msg).setEmbeds(messageEmbed).build()) {
            return new InternalMyMessage(user.openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage(data))
                    .complete());
        }
    }

    @Override
    public MyMessage sendDirectMessage(Component message, Embed... embeds) {
        String msg = ComponentParser.parseComponent(message);

        MessageEmbed[] messageEmbeds = new MessageEmbed[embeds.length];

        for (int i = 0; i < embeds.length; i++) {
            if (!(embeds[i] instanceof InternalEmbed internal))
                throw new IllegalArgumentException("Embed was not created using the EmbedBuilder");

            messageEmbeds[i] = internal.embed();
        }

        try (MessageCreateData data = new MessageCreateBuilder().setContent(msg).setEmbeds(messageEmbeds).build()) {
            return new InternalMyMessage(user.openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage(data))
                    .complete());
        }
    }
}
