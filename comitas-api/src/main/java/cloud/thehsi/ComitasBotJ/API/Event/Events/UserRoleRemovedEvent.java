package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface UserRoleRemovedEvent extends Event, UndoableEvent {
    /**
     * Get the revoked role
     *
     * @return The revoked role
     */
    @NotNull
    Role getRole();

    /**
     * Get the member who the role was revoked from
     *
     * @return The member whose role was revoked
     */
    @NotNull
    Member getMember();
}
