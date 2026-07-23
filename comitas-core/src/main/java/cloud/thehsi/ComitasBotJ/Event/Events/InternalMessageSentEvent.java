package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageSentEvent;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalTextChannel;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentParser;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMessage;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

public class InternalMessageSentEvent implements MessageSentEvent {
    private final Message message;
    private final cloud.thehsi.ComitasBotJ.API.Discord.Message.Message iMessage;
    private final net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel;
    private final net.dv8tion.jda.api.entities.Member author;

    public InternalMessageSentEvent(MessageReceivedEvent event) {
        this.message = event.getMessage();
        this.iMessage = new InternalMessage(message);
        this.channel = event.getChannel().asTextChannel();
        this.author = event.getMember();
    }

    private boolean delete = false;

    @Override
    public boolean isDelete() {
        return delete;
    }

    @Override
    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    @Override
    public void deleteMessage() {
        this.delete = true;
    }

    @Override
    public String getRawContent() {
        return message.getContentRaw();
    }

    @Override
    public cloud.thehsi.ComitasBotJ.API.Discord.Message.Message getMessage() {
        return iMessage;
    }

    @Override
    @Nullable
    public Member getAuthor() {
        if (author == null)
            return null;
        return new InternalMember(author);
    }

    @Override
    public TextChannel getChannel() {
        return new InternalTextChannel(channel);
    }

    @Override
    public void reply(Component message) {
        String msg = ComponentParser.parseComponent(message);

        this.message.reply(msg).queue();
    }
}
