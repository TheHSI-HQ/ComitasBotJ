package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Represents a voice or stage channel, including its text chat
 */
@SuppressWarnings("unused")
public interface VoiceChannel extends MessageChannel, GuildChannel {
    /**
     * Get the members currently connected to this channel
     *
     * @return The connected members
     */
    @NotNull
    @Unmodifiable
    List<Member> getMembers();

    /**
     * Get the maximum amount of connected members
     *
     * @return The user limit, 0 if there is none
     */
    int getUserLimit();

    /**
     * Is this a stage channel
     *
     * @return If this is a stage channel
     */
    boolean isStage();
}
