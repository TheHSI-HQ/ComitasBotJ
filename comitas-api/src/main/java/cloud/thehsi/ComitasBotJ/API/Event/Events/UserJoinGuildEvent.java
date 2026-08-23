package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface UserJoinGuildEvent extends Event {
    /**
     * Get the member who joined
     *
     * @return The member who joined
     */
    @NotNull
    Member getMember();

    /**
     * The guild, who the member joined
     *
     * @return The guild, who the member joined
     */
    @NotNull
    Guild getGuild();
}
