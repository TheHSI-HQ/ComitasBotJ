package cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment;

/**
 * An {@link Attachment} attached to a message
 */
@SuppressWarnings("unused")
public interface MessageAttachment extends Attachment {
    /**
     * Delete the attachment
     */
    void delete();
}
