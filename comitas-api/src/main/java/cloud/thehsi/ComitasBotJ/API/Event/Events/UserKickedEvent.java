package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An {@link Event} triggered when a user gets kicked
 */
@SuppressWarnings("unused")
public interface UserKickedEvent extends Event {
    /**
     * Get the user who was banned
     *
     * @return The user who was banned
     */
    @NotNull
    User getUser();

    /**
     * The moderator who kicked the user.
     *
     * @return The moderator who kicked the user
     */
    @Nullable
    Member getModerator();

    /**
     * The guild, who the member was banned from
     *
     * @return The guild, who the member was banned from
     */
    @NotNull
    Guild getGuild();
}
