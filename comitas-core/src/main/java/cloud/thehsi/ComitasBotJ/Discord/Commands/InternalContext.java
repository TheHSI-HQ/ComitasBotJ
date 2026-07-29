package cloud.thehsi.ComitasBotJ.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Context;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalMessageChannel;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentParser;
import cloud.thehsi.ComitasBotJ.Discord.Message.Embeds.InternalEmbed;
import cloud.thehsi.ComitasBotJ.Discord.Message.InternalMyMessage;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import cloud.thehsi.ComitasBotJ.Main;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
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
        return sender;
    }

    @Override
    public MessageChannel channel() {
        return channel;
    }

    @Override
    public Guild guild() {
        return guild;
    }

    @Override
    public String commandName() {
        return commandName;
    }

    @Override
    public MyMessage reply(Component message, boolean ephemeral) {
        if (used)
            if (!ephemeral) {
                logger.warn("[{}] This interaction has already been acknowledged or replied to, using channel.sendMessage(message) instead", commandName());
                return channel.sendMessage(message);
            } else
                throw new IllegalStateException("This interaction has already been acknowledged or replied to. You can only reply or acknowledge an interaction once!");
        else
            used = true;

        String msg = ComponentParser.parseComponent(message);

        return new InternalMyMessage(this.interaction.reply(msg).setEphemeral(ephemeral).complete().retrieveOriginal().complete());
    }

    @Override
    public MyMessage reply(Component message, boolean ephemeral, Embed embed) {
        if (used)
            if (!ephemeral) {
                logger.warn("[{}] This interaction has already been acknowledged or replied to, using channel.sendMessage(message, embed) instead", commandName());
                return channel.sendMessage(message, embed);
            } else
                throw new IllegalStateException("This interaction has already been acknowledged or replied to. You can only reply or acknowledge an interaction once!");
        else
            used = true;

        String msg = ComponentParser.parseComponent(message);

        if (!(embed instanceof InternalEmbed internal))
            throw new IllegalArgumentException("Embed was not created using the EmbedBuilder");

        MessageEmbed messageEmbed = internal.embed();
        try (MessageCreateData data = new MessageCreateBuilder().setContent(msg).setEmbeds(messageEmbed).build()) {
            return new InternalMyMessage(this.interaction.reply(data).setEphemeral(ephemeral).complete().retrieveOriginal().complete());
        }
    }

    @Override
    public MyMessage reply(Component message, boolean ephemeral, Embed... embeds) {
        if (used)
            if (!ephemeral) {
                logger.warn("[{}] This interaction has already been acknowledged or replied to, using channel.sendMessage(message, embeds) instead", commandName());
                return channel.sendMessage(message, embeds);
            } else
                throw new IllegalStateException("This interaction has already been acknowledged or replied to. You can only reply or acknowledge an interaction once!");
        else
            used = true;

        String msg = ComponentParser.parseComponent(message);

        MessageEmbed[] messageEmbeds = new MessageEmbed[embeds.length];

        for (int i = 0; i < embeds.length; i++) {
            if (!(embeds[i] instanceof InternalEmbed internal))
                throw new IllegalArgumentException("Embed was not created using the EmbedBuilder");

            messageEmbeds[i] = internal.embed();
        }

        try (MessageCreateData data = new MessageCreateBuilder().setContent(msg).setEmbeds(messageEmbeds).build()) {
            return new InternalMyMessage(this.interaction.reply(data).setEphemeral(ephemeral).complete().retrieveOriginal().complete());
        }
    }

    @Override
    public MyMessage reply(Component message) {
        return reply(message, false);
    }

    @Override
    public MyMessage reply(Component message, Embed embed) {
        return reply(message, false, embed);
    }

    @Override
    public MyMessage reply(Component message, Embed... embeds) {
        return reply(message, false, embeds);
    }

    @Override
    public MyMessage replyEphemeral(Component message) {
        return reply(message, true);
    }

    @Override
    public MyMessage replyEphemeral(Component message, Embed embed) {
        return reply(message, true, embed);
    }

    @Override
    public MyMessage replyEphemeral(Component message, Embed... embeds) {
        return reply(message, true, embeds);
    }
}
