package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class EmbedTitle {
    public @NotNull
    static final EmbedTitle NONE = new EmbedTitle();

    @Nullable String text = null;
    @Nullable String url = null;

    private EmbedTitle() {
    }

    public EmbedTitle(@NotNull String text) {
        this.text = text;
    }

    public EmbedTitle(@NotNull String text, @NotNull String url) {
        this.text = text;
        this.url = url;
    }

    @Nullable
    public String getText() {
        return text;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Override
    @NotNull
    public String toString() {
        return "EmbedTitle{" +
                "text='" + text + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
