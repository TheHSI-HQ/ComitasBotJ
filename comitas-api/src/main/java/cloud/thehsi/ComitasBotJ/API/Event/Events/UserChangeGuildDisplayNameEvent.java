package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface UserChangeGuildDisplayNameEvent extends Event, UndoableEvent {
    /**
     * Get the member who changed their nickname
     *
     * @return The member who changed their nickname
     */
    Member getMember();

    /**
     * The guild, which the nickname was changed in
     *
     * @return The guild where the nickname was changed
     */
    Guild getGuild();

    /**
     * Get the user's new nickname
     *
     * @return The user's new nickname or null if unset
     */
    @Nullable
    String getNewDisplayName();

    /**
     * Get the user's old nickname
     *
     * @return The user's old nickname or null if unset
     */
    @Nullable
    String getOldDisplayName();
}
