package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ThreadChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InternalTextChannel extends InternalMessageChannel implements TextChannel {
    @NotNull
    final net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel;

    public InternalTextChannel(@NotNull net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel) {
        super(channel);

        this.channel = channel;
    }

    @NotNull
    public net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel() {
        return channel;
    }

    @Override
    public @NotNull Guild getGuild() {
        DebugLogging.action();
        return new InternalGuild(channel.getGuild());
    }

    @Override
    public @NotNull List<ThreadChannel> getThreads() {
        DebugLogging.action();
        return channel.getThreadChannels().stream()
                .map(e -> (ThreadChannel) new InternalThreadChannel(e))
                .toList();
    }

    @Override
    public @NotNull ThreadChannel createThread(@NotNull String title) {
        DebugLogging.action(title);

        return createThread(title, false);
    }

    @Override
    public @NotNull ThreadChannel createThread(@NotNull String title, boolean isPrivate) {
        DebugLogging.action(title, isPrivate);

        return new InternalThreadChannel(channel.createThreadChannel(title, isPrivate).complete());
    }
}
