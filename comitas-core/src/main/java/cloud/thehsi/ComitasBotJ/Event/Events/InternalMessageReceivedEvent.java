package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageReceivedEvent;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalMessageChannel;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMessage;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Nullable;

public class InternalMessageReceivedEvent implements MessageReceivedEvent {
    private final Message message;
    private final cloud.thehsi.ComitasBotJ.API.Discord.Message.Message iMessage;
    private final MessageChannel channel;
    private final Guild guild;
    private final net.dv8tion.jda.api.entities.Member author;
    private boolean delete = false;

    public InternalMessageReceivedEvent(net.dv8tion.jda.api.events.message.MessageReceivedEvent event) {
        this.message = event.getMessage();
        this.iMessage = new InternalMessage(message, this::deleteMessage);

        this.channel = new InternalMessageChannel(event.getChannel());
        if (event.isFromGuild())
            this.guild = new InternalGuild(event.getGuild());
        else
            this.guild = null;
        this.author = event.getMember();
    }

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
    public Component getContent() {
        return iMessage.getContent();
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
    public MessageChannel getChannel() {
        return channel;
    }

    @Override
    public Guild getGuild() {
        return guild;
    }

    @Override
    public MyMessage reply(Component message) {
        return iMessage.reply(message);
    }

    @Override
    public MyMessage reply(MessageData messageData) {
        return iMessage.reply(messageData);
    }
}
