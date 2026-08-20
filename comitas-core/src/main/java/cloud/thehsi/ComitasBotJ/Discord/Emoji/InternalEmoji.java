package cloud.thehsi.ComitasBotJ.Discord.Emoji;

import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.DebugLogging;

@SuppressWarnings("unused")
public record InternalEmoji(net.dv8tion.jda.api.entities.emoji.Emoji emoji) implements Emoji {
    public InternalEmoji(String unicodeEmoji) {
        this(net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode(unicodeEmoji));
    }

    @Override
    public Component asMessageEmbed() {
        DebugLogging.action();
        return Component.raw(emoji.getAsReactionCode());
    }

    @Override
    public String getName() {
        DebugLogging.action();
        return emoji.getName();
    }

    public net.dv8tion.jda.api.entities.emoji.Emoji emoji() {
        return emoji;
    }
}
