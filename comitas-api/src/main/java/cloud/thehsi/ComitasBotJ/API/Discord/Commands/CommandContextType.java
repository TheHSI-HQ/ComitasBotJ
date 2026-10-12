package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * The command context types
 */
@SuppressWarnings("unused")
public enum CommandContextType {
    /**
     * The guild context type
     * <p>
     * Allows the command to be run in guilds
     */
    GUILD("0"),
    /**
     * The bot dm context type
     * <p>
     * Allows the command to be run in direct messages with the bot
     */
    BOT_DM("1"),
    /**
     * The private channel context type
     * <p>
     * Allows the command to be run in any private channel, if the user has the bot added
     */
    PRIVATE_CHANNEL("2");

    private @NotNull
    final String key;

    CommandContextType(@NotNull String key) {
        this.key = key;
    }

    /**
     * @return The identifier of the command conext type
     */
    @NotNull
    @ApiStatus.Internal
    public String getKey() {
        return this.key;
    }
}
