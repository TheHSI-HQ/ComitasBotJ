package cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Used to create / upload an attachment.
 */
@SuppressWarnings("unused")
public interface AttachmentUpload {
    static @NotNull AttachmentUpload from(@NotNull Path path) throws IOException {
        return Comitas.getUtilityBackend().uploadAttachment(path);
    }

    static @NotNull AttachmentUpload from(@NotNull String filename, byte[] data) {
        return Comitas.getUtilityBackend().uploadAttachment(filename, data);
    }

    /**
     * Set if this attachment is a spoiler
     *
     * @param spoiler Should this attachment be a spoiler
     */
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
     */
    @NotNull
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
     */
    @NotNull
    AttachmentUpload setDescription(@NotNull String description);

    /**
     * Converts the attachment into {@link MessageData}
     */
    @NotNull
    MessageData asMessageData();
}
