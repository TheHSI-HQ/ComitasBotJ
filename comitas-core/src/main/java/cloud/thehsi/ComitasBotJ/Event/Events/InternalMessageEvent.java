package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Event.Events.InternalMessageEventImpl;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class InternalMessageEvent implements InternalMessageEventImpl {
    private final Message message;
    private final MessageChannelUnion channel;
    private final User author;

    public InternalMessageEvent(MessageReceivedEvent event) {
        this.message = event.getMessage();
        this.channel = event.getChannel();
        this.author = event.getAuthor();
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
    public void sendInChannel(String message) {
        channel.sendMessage(message).queue();
    }

    @Override
    public void replyToMessage(String message) {
        this.message.reply(message).queue();
    }
}
