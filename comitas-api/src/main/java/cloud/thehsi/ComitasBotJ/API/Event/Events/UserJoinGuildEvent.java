package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;

@SuppressWarnings("unused")
public interface UserJoinGuildEvent extends Event {
    /**
     * Get the member who joined
     *
     * @return The member who joined
     */
    Member getMember();
}
