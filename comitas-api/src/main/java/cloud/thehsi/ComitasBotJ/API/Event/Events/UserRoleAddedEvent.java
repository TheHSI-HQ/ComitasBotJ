package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;

@SuppressWarnings("unused")
public interface UserRoleAddedEvent extends Event, UndoableEvent {
    /**
     * Get the new role
     *
     * @return The new role
     */
    Role getRole();

    /**
     * Get the member who the role was granted to
     *
     * @return The member who received the role
     */
    Member getMember();
}
