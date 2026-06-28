package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserRoleRemovedEvent;

@SuppressWarnings("unused")
public class InternalUserRoleRemovedEvent implements UserRoleRemovedEvent {
    private final Role role;
    private final Member member;
    private boolean undo = false;

    public InternalUserRoleRemovedEvent(Member member, Role role) {
        this.member = member;
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
    public Member getUser() {
        return member;
    }
}
