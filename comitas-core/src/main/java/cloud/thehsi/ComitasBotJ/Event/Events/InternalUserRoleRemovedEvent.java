package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserRoleRemovedEvent;

@SuppressWarnings("unused")
public class InternalUserRoleRemovedEvent implements UserRoleRemovedEvent {
    private final Role role;
    private final User user;
    private boolean undo = false;

    public InternalUserRoleRemovedEvent(User user, Role role) {
        this.user = user;
        this.role = role;
    }

    @Override
    public boolean willUndo() {
        return undo;
    }

    @Override
    public void setUndo(boolean undo) {
        this.undo = undo;
    }

    @Override
    public void undo() {
        undo = true;
    }

    @Override
    public Role getRole() {
        return role;
    }

    @Override
    public User getUser() {
        return user;
    }
}
