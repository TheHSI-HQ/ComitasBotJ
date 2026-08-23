package cloud.thehsi.ComitasBotJ.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.Discord.Message.Actions.ButtonCallbackManager;
import cloud.thehsi.ComitasBotJ.Discord.Message.Actions.IActionRowComponent;
import cloud.thehsi.ComitasBotJ.Discord.Message.Actions.InternalButton;
import cloud.thehsi.ComitasBotJ.Discord.Message.Attachment.InternalAttachment;
import cloud.thehsi.ComitasBotJ.Discord.Message.Attachment.InternalAttachmentUpload;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentParser;
import cloud.thehsi.ComitasBotJ.Discord.Message.Embeds.InternalEmbed;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class MessageDataParser {
    private MessageDataParser() {}

    public static void parse(@NotNull MessageData messageData, @NotNull Consumer<ParsedMessageData> consumer) {
        String msg = ComponentParser.parseComponent(messageData.getContent());

        MessageEmbed[] messageEmbeds = messageData.getEmbeds().stream()
                .map(e -> {
                    if (!(e instanceof InternalEmbed internal))
                        throw new IllegalArgumentException("Embed was not created using the EmbedBuilder");
                    return internal.embed();
                }).toArray(MessageEmbed[]::new);

        List<AttachedFile> attachedFiles = new ArrayList<>();

        messageData.getAttachmentUploads().forEach(e -> {
            if (!(e instanceof InternalAttachmentUpload internal))
                throw new IllegalArgumentException("AttachmentUpload was not created using the AttachmentUpload::from");

            attachedFiles.add(internal.asFileUpload());
        });

        messageData.getAttachments().forEach(e -> {
            if (!(e instanceof InternalAttachment internal))
                throw new IllegalArgumentException("Attachment was not created by Comitas");

            attachedFiles.add(internal.getAttachment());
        });

        List<ActionRowChildComponent> actionRowChildComponents = messageData.getActionRowComponents().stream()
                .map(e -> {
                    if (!(e instanceof IActionRowComponent internal))
                        throw new IllegalArgumentException("ActionRowElement was not created by Comitas");

                    if (e instanceof InternalButton button)
                        ButtonCallbackManager.registerCallback(button);

                    return internal.getAsActionRowChildComponent();
                }).toList();

        try {
            ParsedMessageData parsedMessageData = new ParsedMessageData(
                    msg,
                    messageEmbeds,
                    attachedFiles,
                    actionRowChildComponents
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

    @NotNull
    public static InternalMyMessage send(@NotNull MessageData messageData, @NotNull Function<MessageCreateData, InternalMyMessage> consumer) {
        AtomicReference<InternalMyMessage> result = new AtomicReference<>();
        parse(messageData, data -> {
            try (MessageCreateData createData = new MessageCreateBuilder()
                    .setContent(data.message())
                    .setEmbeds(data.messageEmbeds())
                    .setComponents(
                            data.actionRowChildComponents().isEmpty()
                                    ? List.of()
                                    : List.of(ActionRow.of(data.actionRowChildComponents()))
                    )
                    .setFiles(data.fileUploads())
                    .build()) {

                result.set(consumer.apply(createData));
            }
        });

        return result.get();
    }

    public static void edit(@NotNull Message message, @NotNull MessageData messageData) {
        parse(messageData, data -> message.editMessage(MessageEditBuilder.fromMessage(message)
                .setContent(data.message())
                .setEmbeds(data.messageEmbeds())
                .setComponents(
                        data.actionRowChildComponents().isEmpty()
                                ? List.of()
                                : List.of(ActionRow.of(data.actionRowChildComponents()))
                )
                .setAttachments(data.attachedFiles())
                .build()).complete());
    }

    public record ParsedMessageData(@NotNull String message, @NotNull MessageEmbed[] messageEmbeds,
                                    @NotNull @Unmodifiable List<AttachedFile> attachedFiles,
                                    @NotNull @Unmodifiable List<ActionRowChildComponent> actionRowChildComponents) {
        @NotNull
        public List<FileUpload> fileUploads() {
            return attachedFiles.stream()
                    .filter(e -> e instanceof FileUpload)
                    .map(e -> (FileUpload) e)
                    .toList();
        }
    }
}
