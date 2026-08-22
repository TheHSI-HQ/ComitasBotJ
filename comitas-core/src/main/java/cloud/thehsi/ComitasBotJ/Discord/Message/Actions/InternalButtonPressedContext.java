package cloud.thehsi.ComitasBotJ.Discord.Message.Actions;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.InteractionAlreadyUsedException;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ButtonPressedContext;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalMessageChannel;
import cloud.thehsi.ComitasBotJ.Discord.InternalInteractionContext;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMyMessage;
import cloud.thehsi.ComitasBotJ.Discord.Message.MessageDataParser;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalButtonPressedContext extends InternalInteractionContext implements ButtonPressedContext {
    final ButtonInteraction interaction;
    final InternalMessageChannel channel;
    final String id;
    final MyMessage myMessage;
    private final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Message.Action.ButtonPressedContext");
    boolean used = false;

    public InternalButtonPressedContext(ButtonInteractionEvent event, String id) {
        super(event.getInteraction());

        this.interaction = event.getInteraction();
        this.id = id;
        this.myMessage = new InternalMyMessage(event.getMessage());
        this.channel = new InternalMessageChannel(event.getChannel());
    }

    @Override
    public String getButtonId() {
        DebugLogging.action();
        return id;
    }

    @Override
    public MyMessage getMessage() {
        DebugLogging.action();
        return myMessage;
    }

    @Override
    public void acknowledge() {
        DebugLogging.action();
        if (used)
            return;
        used = true;
        interaction.deferReply().complete();
    }

    @Override
    public MessageChannel getChannel() {
        DebugLogging.action();
        return channel;
    }

    @Override
    public MyMessage reply(Component message, boolean ephemeral) throws InteractionAlreadyUsedException {
        DebugLogging.action(message, ephemeral);
        return reply(message.asMessageData(), ephemeral);
    }

    @Override
    public MyMessage reply(MessageData messageData, boolean ephemeral) throws InteractionAlreadyUsedException {
        DebugLogging.action(messageData, ephemeral);
        if (used)
            if (!ephemeral) {
                logger.warn("This button pressed interaction has already been acknowledged or replied to, using channel.sendMessage(messageData) instead");
                return channel.sendMessage(messageData);
            } else
                throw new InteractionAlreadyUsedException("This interaction has already been acknowledged or replied to. You can only reply or acknowledge an interaction once!");
        else
            used = true;

        return MessageDataParser.send(messageData, data -> new InternalMyMessage(
                this.interaction.reply(data).setEphemeral(ephemeral).complete().retrieveOriginal().complete()
        ));
    }

    @Override
    public MyMessage reply(Component message) throws InteractionAlreadyUsedException {
        DebugLogging.action(message);
        return reply(message, false);
    }

    @Override
    public MyMessage reply(MessageData messageData) throws InteractionAlreadyUsedException {
        DebugLogging.action(messageData);
        return reply(messageData, false);
    }

    @Override
    public MyMessage replyEphemeral(Component message) throws InteractionAlreadyUsedException {
        DebugLogging.action(message);
        return reply(message, true);
    }

    @Override
    public MyMessage replyEphemeral(MessageData messageData) throws InteractionAlreadyUsedException {
        DebugLogging.action(messageData);
        return reply(messageData, true);
    }
}
