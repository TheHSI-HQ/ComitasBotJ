package cloud.thehsi.ComitasBotJ.Discord.Emoji;

import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;

public record InternalEmoji(net.dv8tion.jda.api.entities.emoji.Emoji emoji) implements Emoji {
    @Override
    public String asMessageEmbed() {
        return emoji.getAsReactionCode();
    }

    @Override
    public String getName() {
        return emoji.getName();
    }
}
