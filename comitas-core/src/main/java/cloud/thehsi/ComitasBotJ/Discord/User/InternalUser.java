package cloud.thehsi.ComitasBotJ.Discord.User;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMyMessage;
import cloud.thehsi.ComitasBotJ.Discord.Message.MessageDataParser;

public class InternalUser implements User {
    public final net.dv8tion.jda.api.entities.User user;

    public InternalUser(net.dv8tion.jda.api.entities.User user) {
        this.user = user;
    }

    @Override
    public String getUserName() {
        return user.getName();
    }

    @Override
    public String getDisplayName() {
        return user.getEffectiveName();
    }

    @Override
    public Long getId() {
        return user.getIdLong();
    }

    @Override
    public boolean isBot() {
        return user.isBot();
    }

    @Override
    public boolean isMe() {
        return user.getIdLong() == user.getJDA().getSelfUser().getIdLong();
    }

    @Override
    public String getLoggableName() {
        return getDisplayName();
    }

    @Override
    public Component mention() {
        return Component.raw(user.getAsMention());
    }

    @Override
    public MyMessage sendDirectMessage(Component message) {
        return sendDirectMessage(message.asMessageData());
    }

    @Override
    public MyMessage sendDirectMessage(MessageData messageData) {
        return MessageDataParser.send(messageData, data -> new InternalMyMessage(user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(data))
                .complete()));
    }
}
