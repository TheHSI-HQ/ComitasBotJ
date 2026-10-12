package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An embed title
 */
@SuppressWarnings("unused")
public class EmbedTitle {
    /**
     * No title
     */
    public @NotNull
    static final EmbedTitle NONE = new EmbedTitle();

    @Nullable String text = null;
    @Nullable String url = null;

    private EmbedTitle() {
    }

    /**
     * Generate an embed title
     *
     * @param text The title
     */
    public EmbedTitle(@NotNull String text) {
        this.text = text;
    }

    /**
     * Generate an embed title with a url
     *
     * @param text The title
     * @param url  The url
     */
    public EmbedTitle(@NotNull String text, @NotNull String url) {
        this.text = text;
        this.url = url;
    }

    /**
     * @return The embed text
     */
    @Nullable
    public String getText() {
        return text;
    }

    /**
     * @return The embed url if present
     */
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
