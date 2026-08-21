package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageEditedEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalMessageChannel;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMessage;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.Nullable;

public class InternalMessageEditedEvent implements MessageEditedEvent {
    private final Message message;
    private final cloud.thehsi.ComitasBotJ.API.Discord.Message.Message iMessage;
    private final MessageChannel channel;
    private final Guild guild;
    private final net.dv8tion.jda.api.entities.Member author;
    private boolean delete = false;

    public InternalMessageEditedEvent(net.dv8tion.jda.api.events.message.MessageUpdateEvent event) {
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
    public boolean markedForDeletion() {
        DebugLogging.action();
        return delete;
    }

    @Override
    public void setDelete(boolean delete) {
        DebugLogging.action(delete);
        this.delete = delete;
    }

    @Override
    public void deleteMessage() {
        DebugLogging.action();
        this.delete = true;
    }

    @Override
    public String getRawContent() {
        DebugLogging.action();
        return message.getContentRaw();
    }

    @Override
    public Component getContent() {
        DebugLogging.action();
        return iMessage.getContent();
    }

    @Override
    public cloud.thehsi.ComitasBotJ.API.Discord.Message.Message getMessage() {
        DebugLogging.action();
        return iMessage;
    }

    @Override
    @Nullable
    public Member getAuthor() {
        DebugLogging.action();
        if (author == null)
            return null;
        return new InternalMember(author);
    }

    @Override
    public MessageChannel getChannel() {
        DebugLogging.action();
        return channel;
    }

    @Override
    public Guild getGuild() {
        DebugLogging.action();
        return guild;
    }

    @Override
    public MyMessage reply(Component message) {
        DebugLogging.action(message);
        return iMessage.reply(message);
    }

    @Override
    public MyMessage reply(MessageData messageData) {
        DebugLogging.action(messageData);
        return iMessage.reply(messageData);
    }
}
