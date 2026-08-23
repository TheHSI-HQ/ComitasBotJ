package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.API.Event.EventOrigin;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserUnbannedEvent;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import org.jetbrains.annotations.NotNull;

public class InternalUserUnbannedEvent extends InternalUndoableEvent implements UserUnbannedEvent {
    final @NotNull GuildUnbanEvent event;
    final @NotNull EventOrigin eventOrigin;

    public InternalUserUnbannedEvent(@NotNull GuildUnbanEvent event, @NotNull EventOrigin origin) {
        this.event = event;
        this.eventOrigin = origin;
    }

    @Override
    public @NotNull User getUser() {
        return new InternalUser(event.getUser());
    }

    @Override
    public @NotNull Guild getGuild() {
        return new InternalGuild(event.getGuild());
    }

    @Override
    public @NotNull EventOrigin getOrigin() {
        return eventOrigin;
    }
}
