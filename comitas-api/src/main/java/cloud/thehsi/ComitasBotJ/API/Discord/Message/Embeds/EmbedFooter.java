package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An embeds footer
 */
@SuppressWarnings("unused")
public class EmbedFooter {
    /**
     * No footer
     */
    public @NotNull
    static final EmbedFooter NONE = new EmbedFooter();

    @Nullable String text = null;
    @Nullable String imageUrl = null;

    private EmbedFooter() {
    }

    /**
     * Create a new footer with text
     *
     * @param text The footer text
     */
    public EmbedFooter(@NotNull String text) {
        this.text = text;
    }

    /**
     * Create a new footer with text and an image url
     *
     * @param text     The footer text
     * @param imageUrl The footer image url
     */
    public EmbedFooter(@NotNull String text, @NotNull String imageUrl) {
        this.text = text;
        this.imageUrl = imageUrl;
    }

    /**
     * @return The footer text
     */
    @Nullable
    public String getText() {
        return text;
    }

    /**
     * @return The footer image url
     */
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
