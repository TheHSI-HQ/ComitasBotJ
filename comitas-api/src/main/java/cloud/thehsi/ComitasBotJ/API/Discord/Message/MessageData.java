package cloud.thehsi.ComitasBotJ.API.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ActionRowComponent;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.Attachment;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.AttachmentUpload;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class MessageData {
    Component content = Component.empty();
    final List<Embed> embeds = new ArrayList<>();
    final List<Attachment> attachments = new ArrayList<>();
    final List<AttachmentUpload> attachmentUploads = new ArrayList<>();
    final List<ActionRowComponent> actionRowComponents = new ArrayList<>();

    public MessageData setContent(Component content) {
        this.content = content;
        return this;
    }

    public MessageData addEmbed(Embed embed) {
        this.embeds.add(embed);
        return this;
    }

    public MessageData addActionRowComponent(ActionRowComponent actionRowComponent) {
        this.actionRowComponents.add(actionRowComponent);
        return this;
    }

    public MessageData addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
        return this;
    }

    public MessageData addAttachment(AttachmentUpload attachment) {
        this.attachmentUploads.add(attachment);
        return this;
    }

    public Component getContent() {
        return content;
    }

    public List<Embed> getEmbeds() {
        return embeds;
    }

    public List<AttachmentUpload> getAttachmentUploads() {
        return attachmentUploads;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public List<ActionRowComponent> getActionRowComponents() {
        return actionRowComponents;
    }

    public MessageData() {
    }

    public MessageData(Component component) {
        this.content = component;
    }

    public MessageData(Component component, Embed... embeds) {
        this.content = component;
        this.embeds.addAll(Arrays.stream(embeds).toList());
    }

    public MessageData(Component component, AttachmentUpload... attachments) {
        this.content = component;
        this.attachmentUploads.addAll(Arrays.stream(attachments).toList());
    }

    public MessageData(Embed... embeds) {
        this.embeds.addAll(Arrays.stream(embeds).toList());
    }

    public MessageData(AttachmentUpload... attachments) {
        this.attachmentUploads.addAll(Arrays.stream(attachments).toList());
    }

    @Override
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

