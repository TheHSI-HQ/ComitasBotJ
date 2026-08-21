package cloud.thehsi.ComitasBotJ.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Message;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageHistory;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalMessageChannel;

import java.util.List;

public record InternalMessageHistory(
        net.dv8tion.jda.api.entities.MessageHistory messageHistory) implements MessageHistory {

    @Override
    public MessageChannel getChannel() {
        DebugLogging.action();
        return new InternalMessageChannel(messageHistory.getChannel());
    }

    @Override
    public List<Message> retrieve(int amount) {
        DebugLogging.action(amount);
        return messageHistory.retrievePast(amount).complete().stream()
                .map(e -> (Message) new InternalMessage(e))
                .toList();
    }
}
