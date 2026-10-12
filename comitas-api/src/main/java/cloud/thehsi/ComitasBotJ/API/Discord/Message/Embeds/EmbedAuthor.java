package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represens the author for an {@link Embed}
 */
@SuppressWarnings("unused")
public class EmbedAuthor {
    /**
     * No author
     */
    public @NotNull
    static final EmbedAuthor NONE = new EmbedAuthor();

    @Nullable String name = null;
    @Nullable String url = null;
    @Nullable String imageUrl = null;

    private EmbedAuthor() {
    }

    /**
     * Create a new author from a name
     *
     * @param name The author name
     */
    public EmbedAuthor(@NotNull String name) {
        this.name = name;
    }

    /**
     * Create a new author from a name and a url
     *
     * @param name The author name
     * @param url  The author url
     */
    public EmbedAuthor(@NotNull String name, @NotNull String url) {
        this.name = name;
        this.url = url;
    }

    /**
     * Create a new author from a name, a url and an image url
     *
     * @param name The author name
     * @param url The author url
     * @param imageUrl The author image url
     */
    public EmbedAuthor(@NotNull String name, @NotNull String url, @NotNull String imageUrl) {
        this.name = name;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    /**
     * @return The authors name if present
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * @return The authors url if present
     */
    @Nullable
    public String getUrl() {
        return url;
    }

    /**
     * @return The authors image url if present
     */
    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    @NotNull
    public String toString() {
        return "EmbedAuthor{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
