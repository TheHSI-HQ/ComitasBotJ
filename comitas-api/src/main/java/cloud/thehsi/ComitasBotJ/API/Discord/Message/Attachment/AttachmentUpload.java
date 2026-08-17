package cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Used to create / upload an attachment.
 */
@SuppressWarnings("unused")
public interface AttachmentUpload {
    static AttachmentUpload from(Path path) throws IOException {
        return Comitas.getUtilityBackend().uploadAttachment(path);
    }

    /**
     * Set if this attachment is a spoiler
     *
     * @param spoiler Should this attachment be a spoiler
     */
    AttachmentUpload setSpoiler(boolean spoiler);

    /**
     * Set the attachments filename
     *
     * @param fileName New filename of this attachment
     */
    AttachmentUpload setFileName(String fileName);

    /**
     * Set the attachments description
     *
     * @param description New description of this attachment
     */
    AttachmentUpload setDescription(String description);

    /**
     * Is the attachment marked as a spoiler
     *
     * @return Is attachment a spoiler
     */
    boolean isSpoiler();

    /**
     * Get the attachments filename
     *
     * @return Filename of this attachment
     */
    String getFileName();

    /**
     * Get the attachments description
     *
     * @return Description of this attachment
     */
    String getDescription();

    /**
     * Converts the attachment into {@link MessageData}
     */
    MessageData asMessageData();
}
