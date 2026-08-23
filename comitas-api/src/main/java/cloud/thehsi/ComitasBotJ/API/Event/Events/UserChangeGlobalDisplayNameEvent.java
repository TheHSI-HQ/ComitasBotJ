package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface UserChangeGlobalDisplayNameEvent extends Event {
    /**
     * Get the user who changed their nickname
     *
     * @return The user who changed their nickname
     */
    @NotNull
    User getUser();

    /**
     * Get the user's new nickname
     *
     * @return The user's new nickname
     */
    @Nullable
    String getNewDisplayName();

    /**
     * Get the user's old nickname
     *
     * @return The user's old nickname
     */
    @Nullable
    String getOldDisplayName();
}
