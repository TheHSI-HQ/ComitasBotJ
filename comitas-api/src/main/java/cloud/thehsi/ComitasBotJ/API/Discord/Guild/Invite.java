package cloud.thehsi.ComitasBotJ.API.Discord.Guild;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

@SuppressWarnings("unused")
public interface Invite {
    /**
     * Delete the invite
     */
    void delete();

    /**
     * Get the channel the invite is targeting if present
     *
     * @return The channel the invite is for if present
     */
    @Nullable
    Channel getChannel();

    /**
     * Get the guild the invite is for
     *
     * @return The inite's guild
     */
    Guild getGuild();

    /**
     * Get the invite code
     *
     * @return The invite code
     */
    String getCode();

    /**
     * Get the invite URL
     *
     * @return The invite URL
     */
    String getUrl();

    /**
     * Get the user who created the invite
     *
     * @return The invite creator
     */
    User getInviter();

    /**
     * Get the invites max age in Seconds
     *
     * @return The invite's max age (in Seconds)
     */
    int getMaxAge();

    /**
     * Get the max amount of uses for the invite
     *
     * @return The invite's max uses
     */
    int getMaxUses();

    /**
     * Get the time, when the invite was created
     *
     * @return The invite's creation time
     */
    OffsetDateTime getTimeCreated();

    /**
     * Get the time, when the invite expires
     *
     * @return The invite's expiry time
     */
    OffsetDateTime getExpiryTime();

    /**
     * Get the invite's current uses
     *
     * @return Uses left on invite
     */
    int getUses();
}
