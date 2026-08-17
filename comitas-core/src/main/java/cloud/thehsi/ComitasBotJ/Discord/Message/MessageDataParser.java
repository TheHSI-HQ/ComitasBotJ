package cloud.thehsi.ComitasBotJ.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.Discord.Message.Attachment.InternalAttachment;
import cloud.thehsi.ComitasBotJ.Discord.Message.Attachment.InternalAttachmentUpload;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentParser;
import cloud.thehsi.ComitasBotJ.Discord.Message.Embeds.InternalEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class MessageDataParser {
    private MessageDataParser() {}

    public record ParsedMessageData(String message, MessageEmbed[] messageEmbeds, List<AttachedFile> attachedFiles) {
        List<FileUpload> fileUploads() {
            List<FileUpload> fileUploads = new ArrayList<>();
            attachedFiles.stream().filter(e -> e instanceof FileUpload).forEach(e -> fileUploads.add((FileUpload) e));
            return fileUploads;
        }
    }

    public static void parse(MessageData messageData, Consumer<ParsedMessageData> consumer) {
        String msg = ComponentParser.parseComponent(messageData.content());

        MessageEmbed[] messageEmbeds = new MessageEmbed[messageData.embeds().size()];

        for (int i = 0; i < messageData.embeds().size(); i++)
            if (!(messageData.embeds().get(i) instanceof InternalEmbed internal))
                throw new IllegalArgumentException("Embed was not created using the EmbedBuilder");
            else
                messageEmbeds[i] = internal.embed();

        List<AttachedFile> attachedFiles = new ArrayList<>();

        for (int i = 0; i < messageData.attachmentUploads().size(); i++) {
            if (!(messageData.attachmentUploads().get(i) instanceof InternalAttachmentUpload internal))
                throw new IllegalArgumentException("AttachmentUpload was not created using the AttachmentUpload::from");

            attachedFiles.add(internal.asFileUpload());
        }

        for (int i = 0; i < messageData.attachments().size(); i++) {
            if (!(messageData.attachments().get(i) instanceof InternalAttachment internal))
                throw new IllegalArgumentException("Attachment was not created by Comitas");

            attachedFiles.add(internal.getAttachment());
        }

        try {
            ParsedMessageData parsedMessageData = new ParsedMessageData(
                    msg,
                    messageEmbeds,
                    attachedFiles
            );
            consumer.accept(parsedMessageData);
        } finally {
            for (AttachedFile attachedFile : attachedFiles) {
                try {
                    attachedFile.close();
                } catch (IOException e) {
                    Comitas.getPluginManager().getPlugin().getLogger().error("Error closing attachment: {}", attachedFile, e);
                }
            }
        }
    }

    public static InternalMyMessage send(MessageData messageData, Function<MessageCreateData, InternalMyMessage> consumer) {
        AtomicReference<InternalMyMessage> result = new AtomicReference<>();
        parse(messageData, data -> {
            try (MessageCreateData createData = new MessageCreateBuilder()
                    .setContent(data.message())
                    .setEmbeds(data.messageEmbeds())
                    .setFiles(data.fileUploads())
                    .build()) {

                result.set(consumer.apply(createData));
            }
        });

        return result.get();
    }
}
