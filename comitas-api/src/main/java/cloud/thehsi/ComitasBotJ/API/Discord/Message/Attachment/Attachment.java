package cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * An attachment which can be attached to a message
 */
@SuppressWarnings("unused")
public interface Attachment {
    /**
     * Gets the attachment hash
     *
     * @return The attachment's hash
     */
    @NotNull
    CompletableFuture<String> getHash();

    /**
     * Gets the attachment content
     *
     * @return The attachment's content
     */
    @NotNull
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
    @NotNull
    String getFileName();

    /**
     * Gets the URL of the attachment
     *
     * @return The attachment's url
     */
    @NotNull
    String getURL();

    /**
     * Gets the media type of the attachment, e.g. {@code audio/ogg}
     *
     * @return The attachment's content type, or null if Discord did not provide one
     */
    @Nullable
    String getContentType();

    /**
     * Is this attachment a voice message
     *
     * @return Is attachment a voice message
     */
    boolean isVoiceMessage();

    /**
     * Gets the duration of the audio, only present on voice messages
     *
     * @return The duration of the audio, or null if this is not a voice message
     */
    @Nullable
    Duration getDuration();

    /**
     * Gets the waveform of the audio, only present on voice messages.
     * <p>
     * The waveform is a low frequency sampling of up to 256 bytes,
     * each byte being the amplitude (0-255) of one sample.
     *
     * @return A copy of the waveform, or null if this is not a voice message
     */
    @Nullable
    byte[] getWaveform();

    /**
     * Converts the attachment into {@link MessageData}
     *
     * @return The conversion result
     */
    @NotNull
    MessageData asMessageData();
}
