package cloud.thehsi.ComitasBotJ.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment.MessageAttachment;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalMessageChannel;
import cloud.thehsi.ComitasBotJ.Discord.Emoji.InternalEmoji;
import cloud.thehsi.ComitasBotJ.Discord.Message.Attachment.InternalAttachment;
import cloud.thehsi.ComitasBotJ.Discord.Message.Attachment.InternalMessageAttachment;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentUnparser;
import cloud.thehsi.ComitasBotJ.Discord.Message.Embeds.InternalEmbed;
import cloud.thehsi.ComitasBotJ.Discord.Reaction.InternalReaction;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReference;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InternalMessage implements Message {
    final net.dv8tion.jda.api.entities.Message message;
    private boolean deleted = false;

    private Runnable optionalDeletionCallback = null;

    public InternalMessage(net.dv8tion.jda.api.entities.Message message) {
        this.message = message;
    }

    public InternalMessage(net.dv8tion.jda.api.entities.Message message, Runnable deletionCallback) {
        this.optionalDeletionCallback = deletionCallback;
        this.message = message;
    }

    @Override
    public void delete() {
        deleted = true;

        if (optionalDeletionCallback != null)
            optionalDeletionCallback.run();
        else
            message.delete().complete();
    }

    @Override
    public boolean isDeleted() {
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
    public String getRawContent() {
        return message.getContentRaw();
    }

    @Override
    public Component getContent() {
        return ComponentUnparser.unparseComponent(getRawContent());
    }

    @Override
    @Nullable
    public Member getAuthor() {
        net.dv8tion.jda.api.entities.Member member = message.getMember();
        if (member == null)
            return null;
        return new InternalMember(member);
    }

    @Override
    @Nullable
    public MessageChannel getChannel() {
        if (!message.hasChannel())
            return null;

        return new InternalMessageChannel(message.getChannel());
    }

    @Override
    public boolean isReply() {
        MessageReference ref = message.getMessageReference();

        if (ref == null || ref.getMessageIdLong() == 0)
            return false;

        return ref.getType() == MessageReference.MessageReferenceType.DEFAULT;
    }

    @Override
    public boolean isForwarded() {
        MessageReference ref = message.getMessageReference();

        if (ref == null || ref.getMessageIdLong() == 0)
            return false;

        return ref.getType() == MessageReference.MessageReferenceType.FORWARD;
    }

    @Override
    public @Nullable Message getRepliedMessage() {
        MessageReference ref = message.getMessageReference();

        if (ref == null || ref.getMessageIdLong() == 0)
            return null;

        return new InternalMessage(ref.getMessage());
    }

    @Override
    public List<Reaction> getReactions() {
        return message.getReactions().stream()
                .map(e -> (Reaction) new InternalReaction(e, this))
                .toList();
    }

    @Override
    public void react(Emoji emoji) {
        message.addReaction(((InternalEmoji) emoji).emoji()).complete();
    }

    @Override
    public void unreact(Emoji emoji) {
        message.removeReaction(((InternalEmoji) emoji).emoji()).complete();
    }

    @Override
    public List<MessageAttachment> getAttachments() {
        return message.getAttachments().stream()
                .map(e -> (MessageAttachment) new InternalMessageAttachment(e, message))
                .toList();
    }

    @Override
    public MessageData getData() {
        MessageData messageData = new MessageData();

        messageData.setContent(Component.raw(getRawContent()));
        for (MessageEmbed embed : message.getEmbeds())
            messageData.addEmbed(new InternalEmbed(embed));

        for (net.dv8tion.jda.api.entities.Message.Attachment attachment : message.getAttachments())
            messageData.addAttachment(new InternalAttachment(attachment));

        return messageData;
    }

    @Override
    public Embed[] getEmbeds() {
        Embed[] embeds = new Embed[message.getEmbeds().size()];
        for (int i = 0; i < message.getEmbeds().size(); i++)
            embeds[i] = new InternalEmbed(message.getEmbeds().get(i));
        return embeds;
    }

    @Override
    public @Nullable MyMessage forward(MessageChannel channel) {
        if (!(channel instanceof InternalMessageChannel internal))
            throw new IllegalArgumentException("MessageChannel was not created by Comitas");

        return new InternalMyMessage(message.forwardTo(internal.channel()).complete());
    }

    @Override
    public @Nullable MyMessage asMyMessage() {
        Member author = getAuthor();
        if (author == null) return null;

        if (!getAuthor().isMe()) return null;

        return new InternalMyMessage(message, optionalDeletionCallback);
    }

    @Override
    public MyMessage reply(Component message) {
        return reply(message.asMessageData());
    }

    @Override
    public MyMessage reply(MessageData messageData) {
        return MessageDataParser.send(messageData, data -> new InternalMyMessage(
                this.message.reply(data).complete())
        );
    }
}
