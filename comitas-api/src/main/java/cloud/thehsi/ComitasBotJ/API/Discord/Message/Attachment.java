package cloud.thehsi.ComitasBotJ.API.Discord.Message;

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
     * Gets the filename of the attachment
     *
     * @return The attachment's filename
     */
    String getFilename();

    /**
     * Gets the URL of the attachment
     *
     * @return The attachment's url
     */
    String getURL();

    // TODO: Continue Implementation
}
