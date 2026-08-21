package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserUnbannedEvent;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import cloud.thehsi.ComitasBotJ.Discord.User.InternalUser;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;

public class InternalUserUnbannedEvent extends InternalUndoableEvent implements UserUnbannedEvent {
    final GuildUnbanEvent event;

    public InternalUserUnbannedEvent(GuildUnbanEvent event) {
        this.event = event;
    }

    @Override
    public User getUser() {
        return new InternalUser(event.getUser());
    }

    @Override
    public Guild getGuild() {
        return new InternalGuild(event.getGuild());
    }
}
