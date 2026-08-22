package cloud.thehsi.ComitasBotJ.API.Discord;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface InteractionContext {
    /**
     * The user who caused the interaction
     *
     * @return The interaction causing user
     */
    User getUser();

    /**
     * The channel in which the interaction occured
     *
     * @return The iteraction's channel
     */
    @Nullable
    Channel getChannel();

    /**
     * The guild in which the interaction occured
     *
     * @return The interaction's guild
     */
    @Nullable
    Guild getGuild();

    /**
     * Attempt to acknowledge the interaction, to prevent ... didn't respond in time, when no reponse is expected.
     */
    void acknowledge();
}
