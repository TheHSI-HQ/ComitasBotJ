package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class EmbedTitle {
    public static final EmbedTitle NONE = new EmbedTitle();

    String text = null;
    String url = null;

    @Nullable
    public String text() {
        return text;
    }

    @Nullable
    public String url() {
        return url;
    }

    private EmbedTitle() {}

    public EmbedTitle(@NotNull String text) {
        this.text = text;
    }

    public EmbedTitle(@NotNull String text, @NotNull String url) {
        this.text = text;
        this.url = url;
    }
}
