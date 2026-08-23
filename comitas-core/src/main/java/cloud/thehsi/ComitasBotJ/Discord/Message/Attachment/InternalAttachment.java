package cloud.thehsi.ComitasBotJ.Discord.Message.Attachment;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.Attachment;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class InternalAttachment implements Attachment {
    final @NotNull Message.Attachment attachment;

    public InternalAttachment(@NotNull Message.Attachment attachment) {
        this.attachment = attachment;
    }

    @Override
    public @NotNull CompletableFuture<String> getHash() {
        DebugLogging.action();
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
                throw new RuntimeException(e); //TODO: Repalce with custom exception
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<byte[]> getContent() {
        DebugLogging.action();
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
                throw new RuntimeException(e); //TODO: Replace with custom exception
            }
        });
    }

    @Override
    public boolean isSpoiler() {
        DebugLogging.action();
        return attachment.isSpoiler();
    }

    @Override
    @Nullable
    public String getDescription() {
        DebugLogging.action();
        return attachment.getDescription();
    }


    @Override
    public @NotNull String getFileName() {
        DebugLogging.action();
        return attachment.getFileName();
    }

    @Override
    public @NotNull String getURL() {
        DebugLogging.action();
        return attachment.getUrl();
    }

    @Override
    public @NotNull MessageData asMessageData() {
        DebugLogging.action();
        return new MessageData().addAttachment(this);
    }

    @NotNull
    public Message.Attachment getAttachment() {
        DebugLogging.action();
        return attachment;
    }
}
