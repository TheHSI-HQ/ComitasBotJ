package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;

@SuppressWarnings("unused")
public interface UserUnbannedEvent extends Event, UndoableEvent {
    /**
     * Get the user who was banned
     *
     * @return The user who was banned
     */
    User getUser();

    /**
     * The guild, who the member was banned from
     *
     * @return The guild, who the member was banned from
     */
    Guild getGuild();
}
