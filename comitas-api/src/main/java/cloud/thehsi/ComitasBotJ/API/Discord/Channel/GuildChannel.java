package cloud.thehsi.ComitasBotJ.API.Discord.Channel;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import org.jetbrains.annotations.NotNull;

public interface GuildChannel {
    /**
     * Gets the channel's guild
     *
     * @return The channel's guild
     */
    @NotNull Guild getGuild();
}
