package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;

@SuppressWarnings("unused")
public interface UserRoleRemovedEvent extends Event {
    /**
     * Will this role revocation be undone
     *
     * @return The role revocation undo status
     */
    boolean willUndo();

    /**
     * Mark / Unmark the role revocation for undoing
     *
     * @param undo Set the role revocation's marked for undo status
     */
    void setUndo(boolean undo);

    /**
     * Mark the role revocation for undoing
     */
    void undo();

    /**
     * Get the revoked role
     *
     * @return The revoked role
     */
    Role getRole();

    /**
     * Get the User who the role was revoked from
     *
     * @return The User whose role was revoked
     */
    Member getUser();
}
