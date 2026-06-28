package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;

@SuppressWarnings("unused")
public interface UserRoleAddedEvent extends Event {
    /**
     * Will this role addition be undone
     *
     * @return The role addition undo status
     */
    boolean willUndo();

    /**
     * Mark / Unmark the role addition for undoing
     *
     * @param undo Set the role addition's marked for undo status
     */
    void setUndo(boolean undo);

    /**
     * Mark the role addition for undoing
     */
    void undo();

    /**
     * Get the new role
     *
     * @return The new role
     */
    Role getRole();

    /**
     * Get the User who the role was granted to
     *
     * @return The User who received the role
     */
    Member getUser();
}
