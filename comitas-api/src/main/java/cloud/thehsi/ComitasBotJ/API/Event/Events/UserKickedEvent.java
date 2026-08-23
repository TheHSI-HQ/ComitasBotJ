package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface UserKickedEvent extends Event {
    /**
     * Get the user who was banned
     *
     * @return The user who was banned
     */
    @NotNull
    User getUser();

    /**
     * The guild, who the member was banned from
     *
     * @return The guild, who the member was banned from
     */
    @NotNull
    Guild getGuild();
}
