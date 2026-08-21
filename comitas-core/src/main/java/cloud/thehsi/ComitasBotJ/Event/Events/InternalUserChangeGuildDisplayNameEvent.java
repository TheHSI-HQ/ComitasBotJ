package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.EventOrigin;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserChangeGuildDisplayNameEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;

public final class InternalUserChangeGuildDisplayNameEvent extends InternalUndoableEvent implements UserChangeGuildDisplayNameEvent {
    private final Member member;
    private final Guild guild;
    private final String oldName;
    private final String newName;
    private final EventOrigin eventOrigin;

    public InternalUserChangeGuildDisplayNameEvent(Member member, Guild guild, String oldName,
                                                   String newName, EventOrigin origin) {
        this.member = member;
        this.guild = guild;
        this.oldName = oldName;
        this.newName = newName;
        this.eventOrigin = origin;
    }

    @Override
    public Member getMember() {
        DebugLogging.action();
        return member;
    }

    @Override
    public Guild getGuild() {
        DebugLogging.action();
        return guild;
    }

    @Override
    public String getNewDisplayName() {
        DebugLogging.action();
        return newName;
    }

    @Override
    public String getOldDisplayName() {
        DebugLogging.action();
        return oldName;
    }

    public String toString() {
        return "InternalUserChangeGuildDisplayNameEvent[" +
                "member=" + member + ", " +
                "guild=" + guild + ", " +
                "oldName=" + oldName + ", " +
                "newName=" + newName + ']';
    }

    @Override
    public EventOrigin getOrigin() {
        return eventOrigin;
    }
}
