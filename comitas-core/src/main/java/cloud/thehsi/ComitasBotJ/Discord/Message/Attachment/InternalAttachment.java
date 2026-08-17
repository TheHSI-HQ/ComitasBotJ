package cloud.thehsi.ComitasBotJ.Discord.Message.Attachment;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.Attachment;
import net.dv8tion.jda.api.entities.Message;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class InternalAttachment implements Attachment {
    final Message.Attachment attachment;

    public InternalAttachment(Message.Attachment attachment) {
        this.attachment = attachment;
    }

    @Override
    public CompletableFuture<String> getHash() {
        return attachment.getProxy().download().thenApply(input -> {
            try (InputStream is = input) {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");

                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }

                return HexFormat.of().formatHex(digest.digest());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<byte[]> getContent() {
        return attachment.getProxy().download().thenApply(input -> {
            try (InputStream is = input) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                return buffer.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public boolean isSpoiler() {
        return attachment.isSpoiler();
    }

    @Override
    public String getDescription() {
        return attachment.getDescription();
    }


    @Override
    public String getFileName() {
        return attachment.getFileName();
    }

    @Override
    public String getURL() {
        return attachment.getUrl();
    }

    public Message.Attachment getAttachment() {
        return attachment;
    }
}
