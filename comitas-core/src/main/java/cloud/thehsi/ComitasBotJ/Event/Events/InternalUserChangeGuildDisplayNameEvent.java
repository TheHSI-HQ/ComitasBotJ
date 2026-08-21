package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserChangeGuildDisplayNameEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;

public record InternalUserChangeGuildDisplayNameEvent(Member member, Guild guild, String oldName,
                                                      String newName) implements UserChangeGuildDisplayNameEvent {
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
}
