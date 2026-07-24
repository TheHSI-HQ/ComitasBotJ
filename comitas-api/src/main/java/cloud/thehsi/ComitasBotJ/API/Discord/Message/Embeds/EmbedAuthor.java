package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class EmbedAuthor {
    public static final EmbedAuthor NONE = new EmbedAuthor();

    String name = null;
    String url = null;
    String imageUrl = null;

    @Nullable
    public String name() {
        return name;
    }

    @Nullable
    public String url() {
        return url;
    }

    @Nullable
    public String imageUrl() {
        return imageUrl;
    }

    private EmbedAuthor() {}

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
}
