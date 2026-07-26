package cloud.thehsi.ComitasBotJ.Discord.Emoji;

import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;

public record InternalEmoji(net.dv8tion.jda.api.entities.emoji.Emoji emoji) implements Emoji {
    @Override
    public Component asMessageEmbed() {
        return Component.raw(emoji.getAsReactionCode());
    }

    @Override
    public String getName() {
        return emoji.getName();
    }

    public net.dv8tion.jda.api.entities.emoji.Emoji emoji() {
        return emoji;
    }
}
