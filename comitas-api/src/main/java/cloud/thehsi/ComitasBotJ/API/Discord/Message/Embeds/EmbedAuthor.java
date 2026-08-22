package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class EmbedAuthor {
    public static final EmbedAuthor NONE = new EmbedAuthor();

    String name = null;
    String url = null;
    String imageUrl = null;

    private EmbedAuthor() {
    }

    public EmbedAuthor(@NotNull String name) {
        this.name = name;
    }

    public EmbedAuthor(@NotNull String name, @NotNull String url) {
        this.name = name;
        this.url = url;
    }

    public EmbedAuthor(@NotNull String name, @NotNull String url, @NotNull String imageUrl) {
        this.name = name;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String toString() {
        return "EmbedAuthor{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
