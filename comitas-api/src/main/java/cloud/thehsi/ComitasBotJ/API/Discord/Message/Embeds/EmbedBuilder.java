package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;

import java.awt.*;
import java.time.temporal.TemporalAccessor;

@SuppressWarnings("unused")
public class EmbedBuilder {
    private EmbedAuthor author = null;
    private Color color = null;
    private Component description = null;
    private EmbedFooter footer = null;
    private String image = null;
    private String thumbnail = null;
    private EmbedTitle title = null;
    private TemporalAccessor timestamp = null;
    private String url = null;

    public EmbedBuilder setAuthor(EmbedAuthor author) {
        this.author = author;
        return this;
    }
    public EmbedBuilder setAuthor(String author) {
        this.author = new EmbedAuthor(author);
        return this;
    }
    public EmbedBuilder setColor(Color color) {
        this.color = color;
        return this;
    }
    public EmbedBuilder setDescription(Component description) {
        this.description = description;
        return this;
    }
    public EmbedBuilder setFooter(EmbedFooter footer) {
        this.footer = footer;
        return this;
    }
    public EmbedBuilder setFooter(String footer) {
        this.footer = new EmbedFooter(footer);
        return this;
    }
    public EmbedBuilder setImage(String url) {
        this.image = url;
        return this;
    }
    public EmbedBuilder setThumbnail(String url) {
        this.thumbnail = url;
        return this;
    }
    public EmbedBuilder setTitle(EmbedTitle title) {
        this.title = title;
        return this;
    }
    public EmbedBuilder setTitle(String title) {
        this.title = new EmbedTitle(title);
        return this;
    }
    public EmbedBuilder setTimestamp(TemporalAccessor timestamp) {
        this.timestamp = timestamp;
        return this;
    }
    public EmbedBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public Embed build() {
        return Comitas.getUtilityBackend().createEmbed(
                author,
                color,
                description,
                footer,
                image,
                thumbnail,
                title,
                timestamp,
                url
        );
    }
}
