package cloud.thehsi.ComitasBotJ.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment;
import net.dv8tion.jda.api.entities.Message;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.CompletableFuture;

public class InternalAttachment implements Attachment {
    Message.Attachment attachment;

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
    public String getFilename() {
        return "";
    }

    @Override
    public String getURL() {
        return attachment.getUrl();
    }
}
