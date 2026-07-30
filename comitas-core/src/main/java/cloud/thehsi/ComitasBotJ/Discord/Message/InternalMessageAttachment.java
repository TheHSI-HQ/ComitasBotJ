package cloud.thehsi.ComitasBotJ.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageAttachment;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

@SuppressWarnings("unused")
public class InternalMessageAttachment extends InternalAttachment implements MessageAttachment {
    final Message message;

    public InternalMessageAttachment(Message.Attachment attachment, Message message) {
        super(attachment);
        this.message = message;
    }

    @Override
    public void delete() {
        List<Message.Attachment> attachments = new java.util.ArrayList<>(message.getAttachments());

        attachments.remove(attachment);

        message.editMessageAttachments(attachments).complete();
    }
}
