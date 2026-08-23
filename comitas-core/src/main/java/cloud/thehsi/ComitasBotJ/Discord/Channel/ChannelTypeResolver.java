package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.NotNull;

public class ChannelTypeResolver {
    @NotNull
    public static Channel resolve(@NotNull net.dv8tion.jda.api.entities.channel.Channel channel) {
        ChannelType type = ChannelType.fromId(channel.getType().name());

        Channel c = switch (type) {
            case TEXT -> new InternalTextChannel((TextChannel) channel);
            case FORUM -> new InternalForumChannel((ForumChannel) channel);
            case THREAD -> new InternalThreadChannel((ThreadChannel) channel);
            default -> null;
        };

        if (c != null) return c;
        if (type.isThread())
            return new InternalThreadChannel((ThreadChannel) channel);

        if (type.isMessage())
            return new InternalMessageChannel((MessageChannel) channel);

        return new InternalChannel(channel);
    }
}
