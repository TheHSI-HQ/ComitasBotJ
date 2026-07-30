package cloud.thehsi.ComitasBotJ.API.Discord.Guild;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;

import java.time.OffsetDateTime;

@SuppressWarnings("unused")
public interface Invite {
    void delete();

    Channel getChannel();
    Guild getGuild();
    String getCode();
    String getUrl();
    User getInviter();
    int getMaxAge();
    int getMaxUses();
    OffsetDateTime getTimeCreated();
    int getUses();
}
