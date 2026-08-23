package cloud.thehsi.ComitasBotJ.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.MessageAttachment;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalMessageChannel;
import cloud.thehsi.ComitasBotJ.Discord.Emoji.InternalEmoji;
import cloud.thehsi.ComitasBotJ.Discord.Message.Attachment.InternalAttachment;
import cloud.thehsi.ComitasBotJ.Discord.Message.Attachment.InternalMessageAttachment;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentUnparser;
import cloud.thehsi.ComitasBotJ.Discord.Message.Embeds.InternalEmbed;
import cloud.thehsi.ComitasBotJ.Discord.Message.Reaction.InternalReaction;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InternalMessage implements Message {
    public @NotNull
    final net.dv8tion.jda.api.entities.Message message;
    private boolean deleted = false;

    private @Nullable Runnable optionalDeletionCallback = null;

    public InternalMessage(@NotNull net.dv8tion.jda.api.entities.Message message) {
        this.message = message;
    }

    public InternalMessage(@NotNull net.dv8tion.jda.api.entities.Message message, @Nullable Runnable deletionCallback) {
        this.optionalDeletionCallback = deletionCallback;
        this.message = message;
    }

    @Override
    public long getId() {
        return message.getIdLong();
    }

    @Override
    public void delete() {
        DebugLogging.action();
        deleted = true;

        if (optionalDeletionCallback != null)
            optionalDeletionCallback.run();
        else
            message.delete().complete();
    }

    @Override
    public boolean isDeleted() {
        DebugLogging.action();
        if (deleted) return true;

        // Validate the message still exists
        try {
            message.getFlagsRaw();
        } catch (Exception ignored) {
            deleted = true;
            return true;
        }
        return false;
    }

    @Override
    public @NotNull String getRawContent() {
        DebugLogging.action();
        return message.getContentRaw();
    }

    @Override
    public @NotNull Component getContent() {
        DebugLogging.action();
        return ComponentUnparser.unparseComponent(getRawContent());
    }

    @Override
    public @NotNull User getAuthorUser() {
        return new InternalUser(message.getAuthor());
    }

    @Override
    public @Nullable Member getAuthor() {
        DebugLogging.action();
        net.dv8tion.jda.api.entities.Member member = message.getMember();
        if (member == null)
            return null;
        return new InternalMember(member);
    }

    @Override
    public @Nullable MessageChannel getChannel() {
        DebugLogging.action();
        if (!message.hasChannel())
            return null;

        return new InternalMessageChannel(message.getChannel());
    }

    @Override
    public boolean isReply() {
        DebugLogging.action();
        MessageReference ref = message.getMessageReference();

        if (ref == null || ref.getMessageIdLong() == 0)
            return false;

        return ref.getType() == MessageReference.MessageReferenceType.DEFAULT;
    }

    @Override
    public boolean isForwarded() {
        DebugLogging.action();
        MessageReference ref = message.getMessageReference();

        if (ref == null || ref.getMessageIdLong() == 0)
            return false;

        return ref.getType() == MessageReference.MessageReferenceType.FORWARD;
    }

    @Override
    public @Nullable Message getRepliedMessage() {
        DebugLogging.action();
        MessageReference ref = message.getMessageReference();

        if (ref == null || ref.getMessage() == null)
            return null;

        return new InternalMessage(ref.getMessage());
    }

    @Override
    public @NotNull List<Reaction> getReactions() {
        DebugLogging.action();
        return message.getReactions().stream()
                .map(e -> (Reaction) new InternalReaction(e, this))
                .toList();
    }

    @Override
    public void react(@NotNull Emoji emoji) {
        DebugLogging.action(emoji);
        message.addReaction(((InternalEmoji) emoji).emoji()).complete();
    }

    @Override
    public void unreact(@NotNull Emoji emoji) {
        DebugLogging.action(emoji);
        message.removeReaction(((InternalEmoji) emoji).emoji()).complete();
    }

    @Override
    public @NotNull List<MessageAttachment> getAttachments() {
        DebugLogging.action();
        return message.getAttachments().stream()
                .map(e -> (MessageAttachment) new InternalMessageAttachment(e, message))
                .toList();
    }

    @Override
    public @NotNull MessageData getData() {
        DebugLogging.action();
        MessageData messageData = new MessageData();

        messageData.setContent(Component.raw(getRawContent()));
        for (MessageEmbed embed : message.getEmbeds())
            messageData.addEmbed(new InternalEmbed(embed));

        for (net.dv8tion.jda.api.entities.Message.Attachment attachment : message.getAttachments())
            messageData.addAttachment(new InternalAttachment(attachment));

        return messageData;
    }

    @Override
    public @NotNull List<Embed> getEmbeds() {
        DebugLogging.action();
        return message.getEmbeds().stream()
                .map(e -> (Embed) new InternalEmbed(e))
                .toList();
    }

    @Override
    public @Nullable MyMessage forward(@NotNull MessageChannel channel) {
        DebugLogging.action(channel);
        if (!(channel instanceof InternalMessageChannel internal))
            throw new IllegalArgumentException("MessageChannel was not created by Comitas");

        return new InternalMyMessage(message.forwardTo(internal.channel()).complete());
    }

    @Override
    public @Nullable MyMessage asMyMessage() {
        DebugLogging.action();
        Member author = getAuthor();
        if (author == null) return null;

        if (!getAuthor().isMe()) return null;

        return new InternalMyMessage(message, optionalDeletionCallback);
    }

    @Override
    public @NotNull MyMessage reply(@NotNull Component message) {
        DebugLogging.action(message);
        return reply(message.asMessageData());
    }

    @Override
    public @NotNull MyMessage reply(@NotNull MessageData messageData) {
        DebugLogging.action(messageData);
        return MessageDataParser.send(messageData, data -> new InternalMyMessage(
                this.message.reply(data).complete())
        );
    }

    @Override
    @NotNull
    public String toString() {
        return "InternalMessage{" +
                "author=" + getAuthor() +
                ", content=" + getRawContent() +
                '}';
    }
}
