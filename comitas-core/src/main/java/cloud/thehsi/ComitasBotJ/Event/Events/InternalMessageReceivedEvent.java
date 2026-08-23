package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageReceivedEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.ChannelTypeResolver;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMessage;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InternalMessageReceivedEvent implements MessageReceivedEvent {
    private @NotNull
    final Message message;
    private @NotNull
    final cloud.thehsi.ComitasBotJ.API.Discord.Message.Message iMessage;
    private @NotNull
    final MessageChannel channel;
    private @Nullable
    final Guild guild;
    private @Nullable
    final net.dv8tion.jda.api.entities.Member author;
    private boolean delete = false;

    public InternalMessageReceivedEvent(@NotNull net.dv8tion.jda.api.events.message.MessageReceivedEvent event) {
        this.message = event.getMessage();
        this.iMessage = new InternalMessage(message, this::deleteMessage);

        this.channel = (MessageChannel) ChannelTypeResolver.resolve(event.getChannel());
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
    public @NotNull String getRawContent() {
        DebugLogging.action();
        return message.getContentRaw();
    }

    @Override
    public @NotNull Component getContent() {
        DebugLogging.action();
        return iMessage.getContent();
    }

    @Override
    @NotNull
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
    public @NotNull MessageChannel getChannel() {
        DebugLogging.action();
        return channel;
    }

    @Override
    @Nullable
    public Guild getGuild() {
        DebugLogging.action();
        return guild;
    }

    @Override
    public @NotNull MyMessage reply(@NotNull Component message) {
        DebugLogging.action(message);
        return iMessage.reply(message);
    }

    @Override
    public @NotNull MyMessage reply(@NotNull MessageData messageData) {
        DebugLogging.action(messageData);
        return iMessage.reply(messageData);
    }
}
