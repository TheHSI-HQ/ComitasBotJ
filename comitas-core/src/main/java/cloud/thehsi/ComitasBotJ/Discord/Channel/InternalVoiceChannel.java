package cloud.thehsi.ComitasBotJ.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.VoiceChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalMember;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InternalVoiceChannel extends InternalMessageChannel implements VoiceChannel {
    @NotNull
    public final AudioChannel audioChannel;

    /**
     * @param channel A voice or stage channel, both have a text chat
     */
    public InternalVoiceChannel(@NotNull AudioChannel channel) {
        super((MessageChannel) channel);

        this.audioChannel = channel;
    }

    @Override
    public @NotNull Guild getGuild() {
        DebugLogging.action();
        return new InternalGuild(audioChannel.getGuild());
    }

    @Override
    public @NotNull List<Member> getMembers() {
        DebugLogging.action();
        // Taken from the voice state cache, no request needed
        return audioChannel.getMembers().stream()
                .map(member -> (Member) new InternalMember(member))
                .toList();
    }

    @Override
    public int getUserLimit() {
        DebugLogging.action();
        return audioChannel.getUserLimit();
    }

    @Override
    public boolean isStage() {
        DebugLogging.action();
        return audioChannel instanceof StageChannel;
    }
}
