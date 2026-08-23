package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class EmbedBuilder {
    private @NotNull
    final List<EmbedField> fields = new ArrayList<>();
    private @Nullable EmbedAuthor author = null;
    private @Nullable Color color = null;
    private @Nullable Component description = null;
    private @Nullable EmbedFooter footer = null;
    private @Nullable String image = null;
    private @Nullable String thumbnail = null;
    private @Nullable EmbedTitle title = null;
    private @Nullable TemporalAccessor timestamp = null;
    private @Nullable String url = null;

    /**
     * Sets the embeds author.
     *
     * @param author The new author
     * @return The embed builder.
     */
    @NotNull
    public EmbedBuilder setAuthor(@Nullable EmbedAuthor author) {
        this.author = author;
        return this;
    }

    /**
     * Sets the embeds author.
     *
     * @param author The new author
     * @return The embed builder.
     */
    @NotNull
    public EmbedBuilder setAuthor(@Nullable String author) {
        if (author == null)
            this.author = null;
        else
            this.author = new EmbedAuthor(author);
        return this;
    }

    /**
     * Sets the embeds color.
     *
     * @param color The new color
     * @return The embed builder.
     */
    @NotNull
    public EmbedBuilder setColor(@Nullable Color color) {
        this.color = color;
        return this;
    }

    /**
     * Sets the embeds description.
     *
     * @param description The new description
     * @return The embed builder.
     */
    @NotNull
    public EmbedBuilder setDescription(@Nullable Component description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the embeds footer.
     *
     * @param footer The new footer
     * @return The embed builder.
     */
    @NotNull
    public EmbedBuilder setFooter(@Nullable EmbedFooter footer) {
        this.footer = footer;
        return this;
    }

    /**
     * Sets the embeds footer.
     *
     * @param footer The new footer
     * @return The embed builder.
     */
    @NotNull
    public EmbedBuilder setFooter(@Nullable String footer) {
        if (footer == null)
            this.footer = null;
        else
            this.footer = new EmbedFooter(footer);
        return this;
    }

    /**
     * Sets the embeds image.
     *
     * @param url The new image url
     * @return The embed builder.
     */
    @NotNull
    public EmbedBuilder setImage(@Nullable String url) {
        this.image = url;
        return this;
    }

    /**
     * Sets the embeds thumbnail.
     *
     * @param url The new thumbnail url
     * @return The embed builder.
     */
    @NotNull
    public EmbedBuilder setThumbnail(@Nullable String url) {
        this.thumbnail = url;
        return this;
    }

    /**
     * Sets the embeds title.
     *
     * @param title The new title
     * @return The embed builder.
     */
    @NotNull
    public EmbedBuilder setTitle(@Nullable EmbedTitle title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the embeds title.
     *
     * @param title The new title
     * @return The embed builder.
     */
    @NotNull
    public EmbedBuilder setTitle(@Nullable String title) {
        if (title == null)
            this.title = null;
        else
            this.title = new EmbedTitle(title);
        return this;
    }

    /**
     * Sets the embeds timestamp.
     *
     * @param timestamp The new timestamp
     * @return The embed builder.
     */
    @NotNull
    public EmbedBuilder setTimestamp(@Nullable TemporalAccessor timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Sets the embeds url.
     *
     * @param url The new url
     * @return The embed builder.
     */
    @NotNull
    public EmbedBuilder setUrl(@Nullable String url) {
        this.url = url;
        return this;
    }

    /**
     * Add a field.
     *
     * @param field The field to add
     * @return The embed builder.
     */
    @NotNull
    public EmbedBuilder addField(@Nullable EmbedField field) {
        this.fields.add(field);
        return this;
    }

    /**
     * Builds the embed
     *
     * @return The build embed.
     */
    @NotNull
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
                url,
                fields
        );
    }
}
