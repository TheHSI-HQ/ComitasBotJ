package cloud.thehsi.ComitasBotJ.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Context;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalMessageChannel;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMyMessage;
import cloud.thehsi.ComitasBotJ.Discord.Message.MessageDataParser;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalContext implements Context {
    private final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".Context");
    final @Nullable Member sender;
    final MessageChannel channel;
    final Guild guild;
    final String commandName;
    boolean used = false;

    final SlashCommandInteraction interaction;

    public InternalContext(SlashCommandInteraction interaction) {
        this.sender = interaction.getMember() == null ? null : new InternalMember(interaction.getMember());
        this.channel = new InternalMessageChannel(interaction.getMessageChannel());
        this.guild = new InternalGuild(interaction.getGuild());
        this.commandName = interaction.getName();

        this.interaction = interaction;
    }

    @Override
    public Member sender() {
        DebugLogging.action();
        return sender;
    }

    @Override
    public MessageChannel channel() {
        DebugLogging.action();
        return channel;
    }

    @Override
    public Guild guild() {
        DebugLogging.action();
        return guild;
    }

    @Override
    public String commandName() {
        DebugLogging.action();
        return commandName;
    }

    @Override
    public MyMessage reply(Component message, boolean ephemeral) {
        DebugLogging.action(message, ephemeral);
        return reply(message.asMessageData(), ephemeral);
    }

    @Override
    public MyMessage reply(MessageData messageData, boolean ephemeral) {
        DebugLogging.action(messageData, ephemeral);
        if (used)
            if (!ephemeral) {
                logger.warn("[{}] This interaction has already been acknowledged or replied to, using channel.sendMessage(message, embeds) instead", commandName());
                return channel.sendMessage(messageData);
            } else
                throw new IllegalStateException("This interaction has already been acknowledged or replied to. You can only reply or acknowledge an interaction once!");
        else
            used = true;

        return MessageDataParser.send(messageData, data -> new InternalMyMessage(
                this.interaction.reply(data).setEphemeral(ephemeral).complete().retrieveOriginal().complete()
        ));
    }

    @Override
    public MyMessage reply(Component message) {
        DebugLogging.action(message);
        return reply(message, false);
    }

    @Override
    public MyMessage reply(MessageData messageData) {
        DebugLogging.action(messageData);
        return reply(messageData, false);
    }

    @Override
    public MyMessage replyEphemeral(Component message) {
        DebugLogging.action(message);
        return reply(message, true);
    }

    @Override
    public MyMessage replyEphemeral(MessageData messageData) {
        DebugLogging.action(messageData);
        return reply(messageData, true);
    }
}
