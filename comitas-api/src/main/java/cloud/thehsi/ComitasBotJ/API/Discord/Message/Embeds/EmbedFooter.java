package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class EmbedFooter {
    public static final EmbedFooter NONE = new EmbedFooter();

    String text = null;
    String imageUrl = null;

    @Nullable
    public String text() {
        return text;
    }

    @Nullable
    public String imageUrl() {
        return imageUrl;
    }

    private EmbedFooter() {}

    public EmbedFooter(@NotNull String text) {
        this.text = text;
    }

    public EmbedFooter(@NotNull String text, @NotNull String imageUrl) {
        this.text = text;
        this.imageUrl = imageUrl;
    }
}
