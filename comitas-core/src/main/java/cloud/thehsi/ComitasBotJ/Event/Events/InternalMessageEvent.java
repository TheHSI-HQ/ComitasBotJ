package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageEvent;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalTextChannel;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class InternalMessageEvent implements MessageEvent {
    private final Message message;
    private final net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel;
    private final net.dv8tion.jda.api.entities.Member author;

    public InternalMessageEvent(MessageReceivedEvent event) {
        this.message = event.getMessage();
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
    public Member getAuthor() {
        return new InternalMember(author);
    }

    @Override
    public TextChannel getChannel() {
        return new InternalTextChannel(channel);
    }

    @Override
    public void reply(String message) {
        this.message.reply(message).queue();
    }
}
