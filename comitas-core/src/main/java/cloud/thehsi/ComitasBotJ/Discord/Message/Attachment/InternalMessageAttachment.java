package cloud.thehsi.ComitasBotJ.Discord.Message.Attachment;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.MessageAttachment;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public class InternalMessageAttachment extends InternalAttachment implements MessageAttachment {
    @NotNull
    final Message message;

    public InternalMessageAttachment(@NotNull Message.Attachment attachment, @NotNull Message message) {
        super(attachment);
        this.message = message;
    }

    @Override
    public void delete() {
        DebugLogging.action();
        List<Message.Attachment> attachments = new java.util.ArrayList<>(message.getAttachments());

        attachments.remove(attachment);

        message.editMessageAttachments(attachments).complete();
    }
}
