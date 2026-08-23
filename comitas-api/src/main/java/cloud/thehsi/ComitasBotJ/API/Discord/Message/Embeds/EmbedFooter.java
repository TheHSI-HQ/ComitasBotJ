package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class EmbedFooter {
    public @NotNull
    static final EmbedFooter NONE = new EmbedFooter();

    @Nullable String text = null;
    @Nullable String imageUrl = null;

    private EmbedFooter() {
    }

    public EmbedFooter(@NotNull String text) {
        this.text = text;
    }

    public EmbedFooter(@NotNull String text, @NotNull String imageUrl) {
        this.text = text;
        this.imageUrl = imageUrl;
    }

    @Nullable
    public String getText() {
        return text;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    @NotNull
    public String toString() {
        return "EmbedFooter{" +
                "text='" + text + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
