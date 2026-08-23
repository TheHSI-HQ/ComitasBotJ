package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface UserChangeUserNameEvent extends Event {
    /**
     * Get the user who changed their username
     *
     * @return The user who changed their username
     */
    @NotNull
    User getUser();

    /**
     * Get the user's new username
     *
     * @return The user's new username
     */
    @NotNull
    String getNewUserName();

    /**
     * Get the user's old username
     *
     * @return The user's old username
     */
    @NotNull
    String getOldUserName();
}
