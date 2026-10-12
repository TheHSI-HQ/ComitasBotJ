package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a comand type
 * <p>
 * Either a guild install and or a user install
 */
@SuppressWarnings("unused")
public enum CommandType {
    /**
     * The guild install command type
     * <p>
     * Allows the command to be used in guilds installs
     */
    GUILD_INSTALL("0"),
    /**
     * The user install command type
     * <p>
     * Allows the command to be used in user installs
     */
    USER_INSTALL("1");

    private @NotNull
    final String key;

    CommandType(@NotNull String key) {
        this.key = key;
    }

    /**
     * @return The identifier key of the command type
     */
    @ApiStatus.Internal
    @NotNull
    public String getKey() {
        return this.key;
    }
}
