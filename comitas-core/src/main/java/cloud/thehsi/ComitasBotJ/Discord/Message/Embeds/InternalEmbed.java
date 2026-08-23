package cloud.thehsi.ComitasBotJ.Discord.Message.Embeds;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.*;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.temporal.TemporalAccessor;
import java.util.List;

@SuppressWarnings({"unused", "ClassCanBeRecord"})
public class InternalEmbed implements Embed {
    @NotNull
    final MessageEmbed embed;

    public InternalEmbed(
            @NotNull MessageEmbed embed
    ) {
        this.embed = embed;
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
            @Nullable String url,
            @NotNull List<EmbedField> fields
    ) {
        EmbedBuilder build = new EmbedBuilder();
        if (author != null)
            build = build.setAuthor(author.getName(), author.getUrl(), author.getImageUrl());
        if (color != null)
            build = build.setColor(color);
        if (description != null)
            build.setDescription(ComponentParser.parseComponent(description));
        if (footer != null)
            build = build.setFooter(footer.getText(), footer.getImageUrl());
        if (image != null)
            build = build.setImage(image);
        if (thumbnail != null)
            build = build.setThumbnail(thumbnail);
        if (title != null)
            build = build.setTitle(title.getText(), title.getUrl());
        if (timestamp != null)
            build = build.setTimestamp(timestamp);
        if (url != null)
            build = build.setUrl(url);

        for (EmbedField field : fields)
            build = build.addField(ComponentParser.parseComponent(field.getName()), ComponentParser.parseComponent(field.getValue()), field.isInline());

        this.embed = build.build();
    }

    @NotNull
    public MessageEmbed embed() {
        return embed;
    }

    @Override
    public @NotNull MessageData asMessageData() {
        DebugLogging.action();
        return new MessageData(this);
    }
}
