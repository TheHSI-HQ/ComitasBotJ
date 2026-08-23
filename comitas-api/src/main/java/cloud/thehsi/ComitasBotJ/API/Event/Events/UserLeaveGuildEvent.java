package cloud.thehsi.ComitasBotJ.API.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface UserLeaveGuildEvent extends Event {
    /**
     * Get the user who left
     *
     * @return The user who left
     */
    @NotNull
    User getUser();

    /**
     * Get the member who left
     *
     * @return The member who left
     */
    @Nullable
    Member getMember();

    /**
     * The guild, who the member left
     *
     * @return The guild, who the member left
     */
    @NotNull
    Guild getGuild();
}
