package cloud.thehsi.ComitasBotJ.API.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ActionRowComponent;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.Attachment;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.AttachmentUpload;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class MessageData {
    final @NotNull List<Embed> embeds = new ArrayList<>();
    final @NotNull List<Attachment> attachments = new ArrayList<>();
    final @NotNull List<AttachmentUpload> attachmentUploads = new ArrayList<>();
    final @NotNull List<ActionRowComponent> actionRowComponents = new ArrayList<>();
    @NotNull Component content = Component.empty();

    public MessageData(@Nullable Component component) {
        this.setContent(component);
    }

    public MessageData(@Nullable Component component, @NotNull Embed... embeds) {
        this.setContent(content);
        this.embeds.addAll(Arrays.stream(embeds).toList());
    }

    public MessageData(@Nullable Component component, @NotNull AttachmentUpload... attachments) {
        this.setContent(component);
        this.attachmentUploads.addAll(Arrays.stream(attachments).toList());
    }

    public MessageData(@NotNull Embed... embeds) {
        this.embeds.addAll(Arrays.stream(embeds).toList());
    }

    public MessageData(@NotNull AttachmentUpload... attachments) {
        this.attachmentUploads.addAll(Arrays.stream(attachments).toList());
    }

    @NotNull
    public MessageData addEmbed(@NotNull Embed embed) {
        this.embeds.add(embed);
        return this;
    }

    @NotNull
    public MessageData addActionRowComponent(@NotNull ActionRowComponent actionRowComponent) {
        this.actionRowComponents.add(actionRowComponent);
        return this;
    }

    @NotNull
    public MessageData addAttachment(@NotNull Attachment attachment) {
        this.attachments.add(attachment);
        return this;
    }

    @NotNull
    public MessageData addAttachment(@NotNull AttachmentUpload attachment) {
        this.attachmentUploads.add(attachment);
        return this;
    }

    @NotNull
    public Component getContent() {
        return content;
    }

    public MessageData() {
    }

    @NotNull
    public MessageData setContent(@Nullable Component content) {
        this.content = content == null ?
                Component.empty() : content;
        return this;
    }

    @NotNull
    public List<Embed> getEmbeds() {
        return embeds;
    }

    @NotNull
    public List<AttachmentUpload> getAttachmentUploads() {
        return attachmentUploads;
    }

    @NotNull
    public List<Attachment> getAttachments() {
        return attachments;
    }

    @NotNull
    public List<ActionRowComponent> getActionRowComponents() {
        return actionRowComponents;
    }

    @Override
    @NotNull
    public String toString() {
        return "MessageData{" +
                "content=" + content +
                ", embeds=" + embeds +
                ", attachments=" + attachments +
                ", attachmentUploads=" + attachmentUploads +
                ", actionRowComponents=" + actionRowComponents +
                '}';
    }
}

