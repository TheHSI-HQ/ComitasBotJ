package cloud.thehsi.ComitasBotJ.Discord.Emoji;

import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;

public class InternalEmoji implements Emoji {
    net.dv8tion.jda.api.entities.emoji.Emoji emoji;

    public InternalEmoji(net.dv8tion.jda.api.entities.emoji.Emoji emoji) {
        this.emoji = emoji;
    }

    @Override
    public String asMessageEmbed() {
        return emoji.getAsReactionCode();
    }

    @Override
    public String getName() {
        return emoji.getName();
    }
}
