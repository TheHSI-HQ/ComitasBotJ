package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.EventOrigin;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserRoleAddedEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class InternalUserRoleAddedEvent extends InternalUndoableEvent implements UserRoleAddedEvent {
    private @NotNull
    final Role role;
    private @NotNull
    final Member member;
    private @NotNull
    final EventOrigin eventOrigin;

    public InternalUserRoleAddedEvent(@NotNull Member member, @NotNull Role role, @NotNull EventOrigin origin) {
        this.member = member;
        this.role = role;
        this.eventOrigin = origin;
    }

    @Override
    public @NotNull Role getRole() {
        DebugLogging.action();
        return role;
    }

    @Override
    public @NotNull Member getMember() {
        DebugLogging.action();
        return member;
    }

    @Override
    public @NotNull EventOrigin getOrigin() {
        return eventOrigin;
    }
}
