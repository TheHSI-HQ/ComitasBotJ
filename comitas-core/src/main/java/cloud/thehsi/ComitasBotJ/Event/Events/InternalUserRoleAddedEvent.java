package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserRoleAddedEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;

@SuppressWarnings("unused")
public class InternalUserRoleAddedEvent implements UserRoleAddedEvent {
    private final Role role;
    private final Member member;
    private boolean undo = false;

    public InternalUserRoleAddedEvent(Member member, Role role) {
        this.member = member;
        this.role = role;
    }

    @Override
    public boolean willUndo() {
        DebugLogging.action();
        return undo;
    }

    @Override
    public void setUndo(boolean undo) {
        DebugLogging.action(undo);
        this.undo = undo;
    }

    @Override
    public void undo() {
        DebugLogging.action();
        undo = true;
    }

    @Override
    public Role getRole() {
        DebugLogging.action();
        return role;
    }

    @Override
    public Member getUser() {
        DebugLogging.action();
        return member;
    }
}
