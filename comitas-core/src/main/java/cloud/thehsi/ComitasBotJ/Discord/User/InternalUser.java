package cloud.thehsi.ComitasBotJ.Discord.User;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMyMessage;
import cloud.thehsi.ComitasBotJ.Discord.Message.MessageDataParser;
import org.jetbrains.annotations.NotNull;

public class InternalUser implements User {
    public @NotNull
    final net.dv8tion.jda.api.entities.User user;

    public InternalUser(@NotNull net.dv8tion.jda.api.entities.User user) {
        this.user = user;
    }

    @Override
    public @NotNull String getUserName() {
        DebugLogging.action();
        return user.getName();
    }

    @Override
    public @NotNull String getDisplayName() {
        DebugLogging.action();
        return user.getEffectiveName();
    }

    @Override
    public long getId() {
        DebugLogging.action();
        return user.getIdLong();
    }

    @Override
    public boolean isBot() {
        DebugLogging.action();
        return user.isBot();
    }

    @Override
    public boolean isMe() {
        DebugLogging.action();
        return user.getIdLong() == user.getJDA().getSelfUser().getIdLong();
    }

    @Override
    public @NotNull String getLoggableName() {
        DebugLogging.action();
        return getDisplayName();
    }

    @Override
    public @NotNull Component mention() {
        DebugLogging.action();
        return Component.raw(user.getAsMention());
    }

    @Override
    public @NotNull MyMessage sendDirectMessage(@NotNull Component message) {
        DebugLogging.action(message);
        return sendDirectMessage(message.asMessageData());
    }

    @Override
    public @NotNull MyMessage sendDirectMessage(@NotNull MessageData messageData) {
        DebugLogging.action(messageData);
        return MessageDataParser.send(messageData, data -> new InternalMyMessage(user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(data))
                .complete()));
    }
}
