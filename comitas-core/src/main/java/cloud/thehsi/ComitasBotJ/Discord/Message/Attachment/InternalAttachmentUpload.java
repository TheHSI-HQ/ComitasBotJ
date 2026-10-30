package cloud.thehsi.ComitasBotJ.Discord.Message.Attachment;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.AttachmentUpload;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import net.dv8tion.jda.api.utils.FileUpload;
import okhttp3.MediaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("unused")
public class InternalAttachmentUpload implements AttachmentUpload {
    final byte[] data;
    @NotNull String fileName;
    @Nullable String description = null;
    boolean spoiler = false;
    @Nullable MediaType voiceMediaType = null;
    byte[] waveform = null;
    @Nullable Duration voiceDuration = null;

    public InternalAttachmentUpload(@NotNull Path path) throws IOException {
        this.fileName = path.getFileName().toString();
        data = Files.readAllBytes(path);
    }

    public InternalAttachmentUpload(@NotNull String filename, byte[] data) {
        this.fileName = filename;
        this.data = data;
    }

    public InternalAttachmentUpload(@NotNull InternalAttachment attachment) {
        this.fileName = attachment.getFileName();
        byte[] _data = null;
        spoiler = attachment.getFileName().startsWith("SPOILER_");

        try {
            _data = attachment.getContent().get();
        } catch (ExecutionException | InterruptedException e) {
            Comitas.getPluginManager().getPlugin().getLogger().error("Exception occurred whilst fetching Attachment", e);
        }

        data = _data;

        Duration duration = attachment.getDuration();
        byte[] attachmentWaveform = attachment.getWaveform();
        if (duration != null && attachmentWaveform != null) {
            String contentType = attachment.getContentType();
            asVoiceMessage(contentType != null && contentType.startsWith("audio/") ? contentType : "audio/ogg",
                    attachmentWaveform, duration);
        }
    }

    @NotNull
    public FileUpload asFileUpload() {
        FileUpload fileUpload = FileUpload.fromData(data, fileName);

        if (spoiler) fileUpload.asSpoiler();

        if (description != null) fileUpload.setDescription(description);

        if (voiceMediaType != null && voiceDuration != null)
            fileUpload.asVoiceMessage(voiceMediaType, waveform, voiceDuration);

        return fileUpload;
    }

    @Override
    public @NotNull AttachmentUpload asVoiceMessage(@NotNull Duration duration) {
        DebugLogging.action(duration);
        byte[] flatWaveform = new byte[256];
        Arrays.fill(flatWaveform, (byte) 128);
        return asVoiceMessage("audio/ogg", flatWaveform, duration);
    }

    @Override
    public @NotNull AttachmentUpload asVoiceMessage(@NotNull String mimeType, byte[] waveform, @NotNull Duration duration) {
        DebugLogging.action(mimeType, waveform, duration);
        MediaType mediaType = MediaType.parse(mimeType);

        if (mediaType == null || !mediaType.type().equals("audio"))
            throw new IllegalArgumentException("Voice message mime type must be an audio type, got: " + mimeType);
        if (waveform == null || waveform.length == 0 || waveform.length > 256)
            throw new IllegalArgumentException("Voice message waveform must be between 1 and 256 bytes long");
        if (duration.isNegative() || duration.isZero())
            throw new IllegalArgumentException("Voice message duration must be positive");

        this.voiceMediaType = mediaType;
        this.waveform = waveform.clone();
        this.voiceDuration = duration;
        return this;
    }

    @Override
    public boolean isVoiceMessage() {
        DebugLogging.action();
        return voiceMediaType != null;
    }

    @Override
    public @Nullable Duration getDuration() {
        DebugLogging.action();
        return voiceDuration;
    }

    @Override
    public @Nullable byte[] getWaveform() {
        DebugLogging.action();
        return waveform == null ? null : waveform.clone();
    }

    @Override
    public @NotNull AttachmentUpload setSpoiler(boolean spoiler) {
        DebugLogging.action(spoiler);
        this.spoiler = spoiler;
        return this;
    }

    @Override
    public @NotNull AttachmentUpload setFileName(@NotNull String fileName) {
        DebugLogging.action(fileName);
        this.fileName = fileName;
        return this;
    }

    @Override
    public @NotNull AttachmentUpload setDescription(@NotNull String description) {
        DebugLogging.action(description);
        this.description = description;
        return this;
    }

    @Override
    public boolean isSpoiler() {
        DebugLogging.action();
        return spoiler;
    }

    @Override
    public @NotNull String getFileName() {
        DebugLogging.action();
        return fileName;
    }

    @Override
    public @Nullable String getDescription() {
        DebugLogging.action();
        return description;
    }

    @Override
    public @NotNull MessageData asMessageData() {
        DebugLogging.action();
        return new MessageData(this);
    }
}
