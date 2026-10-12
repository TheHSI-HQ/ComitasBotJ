package cloud.thehsi.ComitasBotJ.API.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ActionRowComponent;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.Attachment;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.AttachmentUpload;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A builder for messages, used in sending / editing messages
 */
@SuppressWarnings("unused")
public class MessageData {
    final @NotNull List<Embed> embeds = new ArrayList<>();
    final @NotNull List<Attachment> attachments = new ArrayList<>();
    final @NotNull List<AttachmentUpload> attachmentUploads = new ArrayList<>();
    final @NotNull List<ActionRowComponent> actionRowComponents = new ArrayList<>();
    @NotNull Component content = Component.empty();

    /**
     * @param content The content of the message
     */
    public MessageData(@Nullable Component content) {
        this.setContent(content);
    }

    /**
     * @param content The content of the message
     * @param embeds  Additional embeds to attach to the message
     */
    public MessageData(@Nullable Component content, @NotNull Embed... embeds) {
        this.setContent(content);
        this.embeds.addAll(Arrays.stream(embeds).toList());
    }

    /**
     * @param content     The content of the message
     * @param attachments Additional attachments to attach to the message
     */
    public MessageData(@Nullable Component content, @NotNull AttachmentUpload... attachments) {
        this.setContent(content);
        this.attachmentUploads.addAll(Arrays.stream(attachments).toList());
    }

    /**
     * @param embeds Embeds to attach to the message
     */
    public MessageData(@NotNull Embed... embeds) {
        this.embeds.addAll(Arrays.stream(embeds).toList());
    }

    /**
     * @param attachments Attachments to attach to the message
     */
    public MessageData(@NotNull AttachmentUpload... attachments) {
        this.attachmentUploads.addAll(Arrays.stream(attachments).toList());
    }

    /**
     * Initialize empty message data
     */
    public MessageData() {
    }

    /**
     * Add an embed to the message
     *
     * @param embed The embed to add
     * @return The message data
     */
    @NotNull
    @Contract("_ -> this")
    public MessageData addEmbed(@NotNull Embed embed) {
        this.embeds.add(embed);
        return this;
    }

    /**
     * Add an action row component to the message
     *
     * @param actionRowComponent The action row component to add
     * @return The message data
     */
    @NotNull
    @Contract("_ -> this")
    public MessageData addActionRowComponent(@NotNull ActionRowComponent actionRowComponent) {
        this.actionRowComponents.add(actionRowComponent);
        return this;
    }

    /**
     * Add an attachment to the message
     *
     * @param attachment The attachment to add
     * @return The message data
     */
    @NotNull
    @Contract("_ -> this")
    public MessageData addAttachment(@NotNull Attachment attachment) {
        this.attachments.add(attachment);
        return this;
    }

    /**
     * Add an attachment to the message
     *
     * @param attachment The attachment to add
     * @return The message data
     */
    @NotNull
    @Contract("_ -> this")
    public MessageData addAttachment(@NotNull AttachmentUpload attachment) {
        this.attachmentUploads.add(attachment);
        return this;
    }

    /**
     * @return The message content
     */
    @NotNull
    public Component getContent() {
        return content;
    }

    /**
     * Set the content of the message
     *
     * @param content The new message content
     * @return The message data
     */
    @NotNull
    @Contract("_ -> this")
    public MessageData setContent(@Nullable Component content) {
        this.content = content == null ?
                Component.empty() : content;
        return this;
    }

    /**
     * @return A list of all embeds of this message
     */
    @NotNull
    public List<Embed> getEmbeds() {
        return embeds;
    }

    /**
     * @return A list of all attachment uploads of this message
     */
    @NotNull
    public List<AttachmentUpload> getAttachmentUploads() {
        return attachmentUploads;
    }

    /**
     * @return A list of all attachments of this message
     */
    @NotNull
    public List<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * @return A list of all action row components of this message
     */
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

