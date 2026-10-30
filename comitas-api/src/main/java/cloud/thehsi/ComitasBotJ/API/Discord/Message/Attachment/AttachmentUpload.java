package cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Used to create / upload an attachment.
 */
@SuppressWarnings("unused")
public interface AttachmentUpload {
    /**
     * Generate an {@link AttachmentUpload} from a file on the host
     *
     * @param path The path to the file
     * @return The generated attachment
     * @throws IOException Throws if there is an error, whilst accessing the file
     */
    static @NotNull AttachmentUpload from(@NotNull Path path) throws IOException {
        return Comitas.getUtilityBackend().uploadAttachment(path);
    }

    /**
     * Generate an {@link AttachmentUpload} from a filename and a byte array
     *
     * @param filename The filename of the file
     * @param data     The content of the file
     * @return The generated Attachment
     */
    static @NotNull AttachmentUpload from(@NotNull String filename, byte[] data) {
        return Comitas.getUtilityBackend().uploadAttachment(filename, data);
    }

    /**
     * Generate a voice message {@link AttachmentUpload} from an Ogg Opus file on the host.
     * <p>
     * The duration is read from the file, and the waveform is approximated from the bitrate of the audio.
     * Use {@link #asVoiceMessage(String, byte[], Duration)} to provide your own waveform instead.
     *
     * @param path The path to the Ogg Opus file
     * @return The generated voice message attachment
     * @throws IOException              Throws if there is an error, whilst accessing the file
     * @throws IllegalArgumentException If the file is not a valid Ogg Opus file
     */
    static @NotNull AttachmentUpload makeVoiceMessage(@NotNull Path path) throws IOException {
        return Comitas.getUtilityBackend().uploadVoiceMessage(path);
    }

    /**
     * Generate a voice message {@link AttachmentUpload} from the content of an Ogg Opus file,
     * named {@code voice-message.ogg}.
     *
     * @param data The content of the Ogg Opus file
     * @return The generated voice message attachment
     * @throws IllegalArgumentException If the data is not a valid Ogg Opus file
     * @see #makeVoiceMessage(Path)
     */
    static @NotNull AttachmentUpload makeVoiceMessage(byte[] data) {
        return makeVoiceMessage("voice-message.ogg", data);
    }

    /**
     * Generate a voice message {@link AttachmentUpload} from a filename and the content of an Ogg Opus file.
     *
     * @param filename The filename of the file
     * @param data     The content of the Ogg Opus file
     * @return The generated voice message attachment
     * @throws IllegalArgumentException If the data is not a valid Ogg Opus file
     * @see #makeVoiceMessage(Path)
     */
    static @NotNull AttachmentUpload makeVoiceMessage(@NotNull String filename, byte[] data) {
        return Comitas.getUtilityBackend().uploadVoiceMessage(filename, data);
    }

    /**
     * Set if this attachment is a spoiler
     *
     * @param spoiler Should this attachment be a spoiler
     * @return The object this method was called on
     */
    @Contract("_ -> this")
    @NotNull
    AttachmentUpload setSpoiler(boolean spoiler);

    /**
     * Get the attachments filename
     *
     * @return Filename of this attachment
     */
    @NotNull
    String getFileName();

    /**
     * Set the attachments filename
     *
     * @param fileName New filename of this attachment
     * @return The object this method was called on
     */
    @NotNull
    @Contract("_ -> this")
    AttachmentUpload setFileName(@NotNull String fileName);

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
     * Set the attachments description
     *
     * @param description New description of this attachment
     * @return The object this method was called on
     */
    @NotNull
    @Contract("_ -> this")
    AttachmentUpload setDescription(@NotNull String description);

    /**
     * Turns this attachment into a voice message, using {@code audio/ogg} as the mime type
     * and a flat waveform.
     *
     * @param duration The actual duration of the audio
     * @return The object this method was called on
     * @throws IllegalArgumentException If the duration is not positive
     * @see #asVoiceMessage(String, byte[], Duration)
     */
    @NotNull
    @Contract("_ -> this")
    AttachmentUpload asVoiceMessage(@NotNull Duration duration);

    /**
     * Turns this attachment into a voice message.
     * <p>
     * Voice messages have to be sent on their own: the {@link MessageData} may not contain
     * any content, embeds, action row components or other attachments.
     * Voice messages also cannot be added to an existing message by editing it.
     * <p>
     * Discord expects the audio to be encoded as Ogg Opus ({@code audio/ogg}).
     *
     * @param mimeType The mime type of the audio, should be {@code audio/ogg}
     * @param waveform The waveform of the audio, a low frequency sampling of up to 256 bytes,
     *                 each byte being the amplitude (0-255) of one sample
     * @param duration The actual duration of the audio
     * @return The object this method was called on
     * @throws IllegalArgumentException If the mime type is not an audio type, the waveform is not
     *                                  between 1 and 256 bytes long or the duration is not positive
     */
    @NotNull
    @Contract("_, _, _ -> this")
    AttachmentUpload asVoiceMessage(@NotNull String mimeType, byte[] waveform, @NotNull Duration duration);

    /**
     * Is this attachment a voice message
     *
     * @return Is attachment a voice message
     * @see #asVoiceMessage(String, byte[], Duration)
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
     * Gets the waveform of the audio, only present on voice messages
     *
     * @return A copy of the waveform, or null if this is not a voice message
     */
    @Nullable
    byte[] getWaveform();

    /**
     * Converts the attachment into {@link MessageData}
     *
     * @return The converted message data
     */
    @NotNull
    MessageData asMessageData();
}
