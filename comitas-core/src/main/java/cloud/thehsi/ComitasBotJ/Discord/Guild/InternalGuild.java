package cloud.thehsi.ComitasBotJ.Discord.Guild;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalChannel;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalTextChannel;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;

import java.util.ArrayList;
import java.util.List;

public record InternalGuild(net.dv8tion.jda.api.entities.Guild guild) implements Guild {
    @Override
    public String getName() {
        return guild.getName();
    }

    @Override
    public Long getId() {
        return guild.getIdLong();
    }

    @Override
    public TextChannel getDefaultChannel() {
        return new InternalTextChannel(
                (net.dv8tion.jda.api.entities.channel.concrete.TextChannel) guild.getDefaultChannel()
        );
    }

    @Override
    public List<Member> getMembers() {
        List<Member> members = new ArrayList<>();

        for (net.dv8tion.jda.api.entities.Member member : guild.getMembers()) {
            members.add(new InternalMember(member));
        }

        return members;
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> channels = new ArrayList<>();

        for (net.dv8tion.jda.api.entities.channel.Channel channel : guild.getChannels()) {
            if (channel instanceof net.dv8tion.jda.api.entities.channel.concrete.TextChannel)
                channels.add(new InternalTextChannel((net.dv8tion.jda.api.entities.channel.concrete.TextChannel) channel));
            else
                channels.add(new InternalChannel(channel));
        }

        return channels;
    }
}
