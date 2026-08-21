package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.EventOrigin;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserRoleRemovedEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;

@SuppressWarnings("unused")
public class InternalUserRoleRemovedEvent extends InternalUndoableEvent implements UserRoleRemovedEvent {
    private final Role role;
    private final Member member;
    private final EventOrigin eventOrigin;

    public InternalUserRoleRemovedEvent(Member member, Role role, EventOrigin origin) {
        this.member = member;
        this.role = role;
        this.eventOrigin = origin;
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

    @Override
    public EventOrigin getOrigin() {
        return eventOrigin;
    }
}
