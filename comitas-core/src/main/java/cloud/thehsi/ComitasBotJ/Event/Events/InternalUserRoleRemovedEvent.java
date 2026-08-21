package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserRoleRemovedEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;

@SuppressWarnings("unused")
public class InternalUserRoleRemovedEvent extends InternalUndoableEvent implements UserRoleRemovedEvent {
    private final Role role;
    private final Member member;

    public InternalUserRoleRemovedEvent(Member member, Role role) {
        this.member = member;
        this.role = role;
    }

    @Override
    public Role getRole() {
        DebugLogging.action();
        return role;
    }

    @Override
    public Member getMember() {
        DebugLogging.action();
        return member;
    }
}
