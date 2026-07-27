package cloud.thehsi.ComitasBotJ.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalMessageChannel;
import cloud.thehsi.ComitasBotJ.Discord.Emoji.InternalEmoji;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentParser;
import cloud.thehsi.ComitasBotJ.Discord.Message.Embeds.InternalEmbed;
import cloud.thehsi.ComitasBotJ.Discord.Reaction.InternalReaction;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
    @Nullable
    public Member getAuthor() {
        net.dv8tion.jda.api.entities.Member member = message.getMember();
        if (member == null)
            return null;
        return new InternalMember(member);
    }

    @Override
    public MessageChannel getChannel() {
        return new InternalMessageChannel(message.getChannel());
    }

    @Override
    public boolean isReply() {
        return getRepliedMessage() != null;
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
        List<Reaction> reactions = new ArrayList<>();
        for (MessageReaction reaction : message.getReactions()) {
            reactions.add(new InternalReaction(reaction, this));
        }
        return reactions;
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
    public List<Attachment> getAttachments() {
        List<Attachment> attachments = new ArrayList<>();
        for (net.dv8tion.jda.api.entities.Message.Attachment attachment : message.getAttachments()) {
            attachments.add(new InternalAttachment(attachment));
        }
        return attachments;
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
        String msg = ComponentParser.parseComponent(message);

        return new InternalMyMessage(this.message.reply(msg).complete());
    }

    @Override
    public MyMessage reply(Component message, Embed embed) {
        String msg = ComponentParser.parseComponent(message);

        if (!(embed instanceof InternalEmbed internal))
            throw new IllegalArgumentException("Embed was not created using the EmbedBuilder");

        MessageEmbed messageEmbed = internal.embed();
        try (MessageCreateData data = new MessageCreateBuilder().setContent(msg).setEmbeds(messageEmbed).build()) {
            return new InternalMyMessage(this.message.reply(data).complete());
        }
    }

    @Override
    public MyMessage reply(Component message, Embed... embeds) {
        String msg = ComponentParser.parseComponent(message);

        MessageEmbed[] messageEmbeds = new MessageEmbed[embeds.length];

        for (int i = 0; i < embeds.length; i++) {
            if (!(embeds[i] instanceof InternalEmbed internal))
                throw new IllegalArgumentException("Embed was not created using the EmbedBuilder");

            messageEmbeds[i] = internal.embed();
        }

        try (MessageCreateData data = new MessageCreateBuilder().setContent(msg).setEmbeds(messageEmbeds).build()) {
            return new InternalMyMessage(this.message.reply(data).complete());
        }
    }
}
