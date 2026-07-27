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

    /**
     * Sets the embeds author.
     *
     * @param author The new author
     * @return The embed builder.
     */
    public EmbedBuilder setAuthor(EmbedAuthor author) {
        this.author = author;
        return this;
    }

    /**
     * Sets the embeds author.
     *
     * @param author The new author
     * @return The embed builder.
     */
    public EmbedBuilder setAuthor(String author) {
        this.author = new EmbedAuthor(author);
        return this;
    }

    /**
     * Sets the embeds color.
     *
     * @param color The new color
     * @return The embed builder.
     */
    public EmbedBuilder setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * Sets the embeds description.
     *
     * @param description The new description
     * @return The embed builder.
     */
    public EmbedBuilder setDescription(Component description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the embeds footer.
     *
     * @param footer The new footer
     * @return The embed builder.
     */
    public EmbedBuilder setFooter(EmbedFooter footer) {
        this.footer = footer;
        return this;
    }

    /**
     * Sets the embeds footer.
     *
     * @param footer The new footer
     * @return The embed builder.
     */
    public EmbedBuilder setFooter(String footer) {
        this.footer = new EmbedFooter(footer);
        return this;
    }

    /**
     * Sets the embeds image.
     *
     * @param url The new image url
     * @return The embed builder.
     */
    public EmbedBuilder setImage(String url) {
        this.image = url;
        return this;
    }

    /**
     * Sets the embeds thumbnail.
     *
     * @param url The new thumbnail url
     * @return The embed builder.
     */
    public EmbedBuilder setThumbnail(String url) {
        this.thumbnail = url;
        return this;
    }

    /**
     * Sets the embeds title.
     *
     * @param title The new title
     * @return The embed builder.
     */
    public EmbedBuilder setTitle(EmbedTitle title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the embeds title.
     *
     * @param title The new title
     * @return The embed builder.
     */
    public EmbedBuilder setTitle(String title) {
        this.title = new EmbedTitle(title);
        return this;
    }

    /**
     * Sets the embeds timestamp.
     *
     * @param timestamp The new timestamp
     * @return The embed builder.
     */
    public EmbedBuilder setTimestamp(TemporalAccessor timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Sets the embeds url.
     *
     * @param url The new url
     * @return The embed builder.
     */
    public EmbedBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Builds the embed
     *
     * @return The build embed.
     */
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
