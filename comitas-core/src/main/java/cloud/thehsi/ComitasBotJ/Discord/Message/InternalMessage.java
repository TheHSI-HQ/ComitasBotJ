package cloud.thehsi.ComitasBotJ.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalTextChannel;
import cloud.thehsi.ComitasBotJ.Discord.Reaction.InternalReaction;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.MessageReference;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InternalMessage implements Message {
    private final net.dv8tion.jda.api.entities.Message message;
    private boolean deleted = false;

    public InternalMessage(net.dv8tion.jda.api.entities.Message message) {
        this.message = message;
    }


    @Override
    public void delete() {
        deleted = true;
        message.delete().queue();
    }

    @Override
    public boolean isDeleted() {
        return deleted;
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
    public TextChannel getChannel() {
        return new InternalTextChannel((net.dv8tion.jda.api.entities.channel.concrete.TextChannel) message.getChannel());
    }

    @Override
    public boolean isReply() {
        return getRepliedMessage() != null;
    }

    @Override
    public @Nullable Message getRepliedMessage() {
        MessageReference ref = message.getMessageReference();

        if (ref == null || ref.getMessageIdLong() != 0)
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
    public List<Attachment> getAttachments() {
        List<Attachment> attachments = new ArrayList<>();
        for (net.dv8tion.jda.api.entities.Message.Attachment attachment : message.getAttachments()) {
            attachments.add(new InternalAttachment(attachment));
        }
        return attachments;
    }

    @Override
    public void reply(String message) {

    }
}
