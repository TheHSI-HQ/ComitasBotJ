package cloud.thehsi.ComitasBotJ.API.Discord.Guild;

import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface Ban {
    /**
     * Get the banned user
     *
     * @return The banned user
     */
    @NotNull
    User getUser();

    /**
     * Get the ban reason
     *
     * @return The ban reason if present
     */
    @Nullable
    String getReason();

    /**
     * Get the guild this ban was issued in
     *
     * @return The ban's guild
     */
    @NotNull
    Guild getGuild();

    /**
     * Pardon the ban (Unban the banned member)
     */
    void unban();
}
