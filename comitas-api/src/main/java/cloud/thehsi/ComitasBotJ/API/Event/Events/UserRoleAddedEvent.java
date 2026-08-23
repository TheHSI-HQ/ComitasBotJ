package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface UserRoleAddedEvent extends Event, UndoableEvent {
    /**
     * Get the new role
     *
     * @return The new role
     */
    @NotNull
    Role getRole();

    /**
     * Get the member who the role was granted to
     *
     * @return The member who received the role
     */
    @NotNull
    Member getMember();
}
