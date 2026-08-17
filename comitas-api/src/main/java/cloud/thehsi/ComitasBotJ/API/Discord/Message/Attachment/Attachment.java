package cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public interface Attachment {
    /**
     * Gets the attachment hash
     *
     * @return The attachment's hash
     */
    CompletableFuture<String> getHash();

    /**
     * Gets the attachment content
     *
     * @return The attachment's content
     */
    CompletableFuture<byte[]> getContent();

    /**
     * Is the attachment marked as a spoiler
     *
     * @return Is attachment a spoiler
     */
    boolean isSpoiler();

    /**
     * Get the attachments description
     *
     * @return Description of this attachment
     */
    @Nullable
    String getDescription();

    /**
     * Gets the filename of the attachment
     *
     * @return The attachment's filename
     */
    String getFileName();

    /**
     * Gets the URL of the attachment
     *
     * @return The attachment's url
     */
    String getURL();

    /**
     * Converts the attachment into {@link MessageData}
     */
    MessageData asMessageData();
}
