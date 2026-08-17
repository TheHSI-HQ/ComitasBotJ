package cloud.thehsi.ComitasBotJ.Discord.Message.Attachment;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.AttachmentUpload;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("unused")
public class InternalAttachmentUpload implements AttachmentUpload {
    final byte[] data;
    String fileName;
    String description = null;
    boolean spoiler = false;

    public InternalAttachmentUpload(Path path) throws IOException {
        this.fileName = path.getFileName().toString();
        data = Files.readAllBytes(path);
    }

    public InternalAttachmentUpload(InternalAttachment attachment) {
        this.fileName = attachment.getFileName();
        byte[] _data = null;
        spoiler = attachment.getFileName().startsWith("SPOILER_");

        try {
            _data = attachment.getContent().get();
        } catch (ExecutionException | InterruptedException e) {
            Comitas.getPluginManager().getPlugin().getLogger().error("Exception occurred whilst fetching Attachment", e);
        }

        data = _data;
    }

    public FileUpload asFileUpload() {
        FileUpload fileUpload = FileUpload.fromData(data, fileName);

        if (spoiler) fileUpload = fileUpload.asSpoiler();

        if (description != null)  fileUpload = fileUpload.setDescription(description);

        return fileUpload;
    }

    @Override
    public AttachmentUpload setSpoiler(boolean spoiler) {
        this.spoiler = spoiler;
        return this;
    }

    @Override
    public AttachmentUpload setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Override
    public AttachmentUpload setDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public boolean isSpoiler() {
        return spoiler;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
