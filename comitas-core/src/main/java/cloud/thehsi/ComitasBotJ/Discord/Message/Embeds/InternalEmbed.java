package cloud.thehsi.ComitasBotJ.Discord.Message.Embeds;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedAuthor;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedFooter;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedTitle;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.temporal.TemporalAccessor;

public class InternalEmbed implements Embed {
    final MessageEmbed embed;

    public MessageEmbed embed() {
        return embed;
    }

    public InternalEmbed(
            @Nullable EmbedAuthor author,
            @Nullable Color color,
            @Nullable Component description,
            @Nullable EmbedFooter footer,
            @Nullable String image,
            @Nullable String thumbnail,
            @Nullable EmbedTitle title,
            @Nullable TemporalAccessor timestamp,
            @Nullable String url
    ) {
        EmbedBuilder build = new EmbedBuilder();
        if (author != null)
            build = build.setAuthor(author.name(), author.url(), author.imageUrl());
        if (color != null)
            build = build.setColor(color);
        if (description != null)
            build.setDescription(ComponentParser.parseComponent(description));
        if (footer != null)
            build = build.setFooter(footer.text(), footer.imageUrl());
        if (image != null)
            build = build.setImage(image);
        if (thumbnail != null)
            build = build.setThumbnail(thumbnail);
        if (title != null)
            build = build.setTitle(title.text(), title.url());
        if (timestamp != null)
            build = build.setTimestamp(timestamp);
        if (url != null)
            build = build.setUrl(url);

        this.embed = build.build();
    }
}
