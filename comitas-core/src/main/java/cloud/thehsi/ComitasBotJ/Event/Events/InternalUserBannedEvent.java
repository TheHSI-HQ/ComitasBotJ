package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Ban;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.API.Event.EventOrigin;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserBannedEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalBan;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;

import java.time.OffsetDateTime;
import java.util.Objects;

public class InternalUserBannedEvent extends InternalUndoableEvent implements UserBannedEvent {
    final GuildBanEvent event;
    final Ban ban;
    final EventOrigin eventOrigin;

    public InternalUserBannedEvent(GuildBanEvent event, EventOrigin origin) {
        this.event = event;
        String reason = event.getGuild().retrieveAuditLogs()
                .type(ActionType.BAN)
                .limit(50) // Abituary value that should capture the getReason
                .complete().stream()
                .filter(e -> e.getTargetIdLong() == event.getUser().getIdLong())
                .filter(e -> e.getTimeCreated().isAfter(OffsetDateTime.now().minusMinutes(1)))
                .map(AuditLogEntry::getReason)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        this.ban = new InternalBan(getUser(), reason, getGuild());
        this.eventOrigin = origin;
    }

    @Override
    public User getUser() {
        DebugLogging.action();
        return new InternalUser(event.getUser());
    }

    @Override
    public Guild getGuild() {
        DebugLogging.action();
        return new InternalGuild(event.getGuild());
    }

    @Override
    public Ban getBan() {
        DebugLogging.action();
        return ban;
    }

    @Override
    public EventOrigin getOrigin() {
        return eventOrigin;
    }
}
