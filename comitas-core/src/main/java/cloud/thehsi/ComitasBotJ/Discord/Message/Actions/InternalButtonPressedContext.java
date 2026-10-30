package cloud.thehsi.ComitasBotJ.Discord.Message.Actions;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Interaction.InteractionAlreadyUsedException;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow.ButtonPressedContext;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalMessageChannel;
import cloud.thehsi.ComitasBotJ.Discord.InternalInteractionContext;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMyMessage;
import cloud.thehsi.ComitasBotJ.Discord.Message.MessageDataParser;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalButtonPressedContext extends InternalInteractionContext implements ButtonPressedContext {
    @NotNull
    final ButtonInteraction interaction;
    @NotNull
    final InternalMessageChannel channel;
    @NotNull
    final String id;
    @NotNull
    final MyMessage myMessage;
    private @NotNull
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Message.Action.ButtonPressedContext");
    boolean used = false;

    public InternalButtonPressedContext(@NotNull ButtonInteractionEvent event, @NotNull String id) {
        super(event.getInteraction());

        this.interaction = event.getInteraction();
        this.id = id;
        this.myMessage = new InternalMyMessage(event.getMessage());
        this.channel = new InternalMessageChannel(event.getChannel());
    }

    @Override
    public @NotNull String getButtonId() {
        DebugLogging.action();
        return id;
    }

    @Override
    public @NotNull MyMessage getMessage() {
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
    public @NotNull MessageChannel getChannel() {
        DebugLogging.action();
        return channel;
    }

    @Override
    public @NotNull MyMessage reply(@NotNull Component message, boolean ephemeral) throws InteractionAlreadyUsedException {
        DebugLogging.action(message, ephemeral);
        return reply(message.asMessageData(), ephemeral);
    }

    @Override
    public @NotNull MyMessage reply(@NotNull MessageData messageData, boolean ephemeral) throws InteractionAlreadyUsedException {
        DebugLogging.action(messageData, ephemeral);
        if (used) {
            // Follow up messages keep the ephemeral flag and work in channels the bot can't write to
            if (DebugLogging.isBasicEnabled())
                logger.debug("This button pressed interaction has already been acknowledged or replied to, sending a follow up message");
            return MessageDataParser.send(messageData, data -> new InternalMyMessage(
                    this.interaction.getHook().sendMessage(data).setEphemeral(ephemeral).complete()
            ));
        }
        used = true;

        return MessageDataParser.send(messageData, data -> new InternalMyMessage(
                this.interaction.reply(data).setEphemeral(ephemeral).complete().retrieveOriginal().complete()
        ));
    }

    @Override
    public @NotNull MyMessage reply(@NotNull Component message) throws InteractionAlreadyUsedException {
        DebugLogging.action(message);
        return reply(message, false);
    }

    @Override
    public @NotNull MyMessage reply(@NotNull MessageData messageData) throws InteractionAlreadyUsedException {
        DebugLogging.action(messageData);
        return reply(messageData, false);
    }

    @Override
    public @NotNull MyMessage replyEphemeral(@NotNull Component message) throws InteractionAlreadyUsedException {
        DebugLogging.action(message);
        return reply(message, true);
    }

    @Override
    public @NotNull MyMessage replyEphemeral(@NotNull MessageData messageData) throws InteractionAlreadyUsedException {
        DebugLogging.action(messageData);
        return reply(messageData, true);
    }
}
