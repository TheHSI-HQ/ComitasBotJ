package cloud.thehsi.ComitasBotJ.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandRanContext;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.InteractionAlreadyUsedException;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalMessageChannel;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.InternalInteractionContext;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMyMessage;
import cloud.thehsi.ComitasBotJ.Discord.Message.MessageDataParser;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalCommandRanContext extends InternalInteractionContext implements CommandRanContext {
    final @Nullable Guild guild;
    final @Nullable Member sender;
    final MessageChannel channel;
    private final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Command.CommandRanContext");
    final String commandName;
    boolean used = false;

    final SlashCommandInteraction interaction;

    public InternalCommandRanContext(SlashCommandInteraction interaction) {
        super(interaction);
        this.sender = interaction.getMember() == null ? null : new InternalMember(interaction.getMember());
        this.channel = new InternalMessageChannel(interaction.getMessageChannel());
        this.guild = new InternalGuild(interaction.getGuild());
        this.commandName = interaction.getName();

        this.interaction = interaction;
    }

    @Override
    @Nullable
    public Member getSender() {
        DebugLogging.action();
        return sender;
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
    @Nullable
    public Guild getGuild() {
        DebugLogging.action();
        return guild;
    }

    @Override
    public String getCommandName() {
        DebugLogging.action();
        return commandName;
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
                logger.warn("[{}] This interaction has already been acknowledged or replied to, using channel.sendMessage(messageData) instead", getCommandName());
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
