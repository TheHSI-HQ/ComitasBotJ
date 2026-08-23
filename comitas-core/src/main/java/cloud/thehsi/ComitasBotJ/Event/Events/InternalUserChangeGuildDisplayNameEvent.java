package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.EventOrigin;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserChangeGuildDisplayNameEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InternalUserChangeGuildDisplayNameEvent extends InternalUndoableEvent implements UserChangeGuildDisplayNameEvent {
    private @NotNull
    final Member member;
    private @NotNull
    final Guild guild;
    private @Nullable
    final String oldName;
    private @Nullable
    final String newName;
    private @NotNull
    final EventOrigin eventOrigin;

    public InternalUserChangeGuildDisplayNameEvent(@NotNull Member member, @NotNull Guild guild, @Nullable String oldName,
                                                   @Nullable String newName, @NotNull EventOrigin origin) {
        this.member = member;
        this.guild = guild;
        this.oldName = oldName;
        this.newName = newName;
        this.eventOrigin = origin;
    }

    @Override
    public @NotNull Member getMember() {
        DebugLogging.action();
        return member;
    }

    @Override
    public @NotNull Guild getGuild() {
        DebugLogging.action();
        return guild;
    }

    @Override
    @Nullable
    public String getNewDisplayName() {
        DebugLogging.action();
        return newName;
    }

    @Override
    @Nullable
    public String getOldDisplayName() {
        DebugLogging.action();
        return oldName;
    }

    @Override
    @NotNull
    public String toString() {
        return "InternalUserChangeGuildDisplayNameEvent[" +
                "member=" + member + ", " +
                "guild=" + guild + ", " +
                "oldName=" + oldName + ", " +
                "newName=" + newName + ']';
    }

    @Override
    public @NotNull EventOrigin getOrigin() {
        return eventOrigin;
    }
}
