package cloud.thehsi.ComitasBotJ.API.Discord.User.Presence;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the online status of a user
 */
public enum OnlineStatus {
    /**
     * Online online status
     */
    ONLINE("online"),
    /**
     * Idle online status
     */
    IDLE("idle"),
    /**
     * Do Not Disturb online status
     */
    DO_NOT_DISTURB("dnd"),
    /**
     * Invisible online status
     */
    INVISIBLE("invisible"),
    /**
     * Offline online status
     */
    OFFLINE("offline"),
    /**
     * Unknown online status
     */
    UNKNOWN("");

    private @NotNull
    final String key;

    OnlineStatus(@NotNull String key) {
        this.key = key;
    }

    /**
     * Resolves an online status from a key
     *
     * @param key The online status key to resolve
     * @return The resolved status or UNKNOWN
     */
    public @NotNull
    static OnlineStatus fromKey(@NotNull String key) {
        for(OnlineStatus onlineStatus : values()) {
            if (onlineStatus.key.equalsIgnoreCase(key)) {
                return onlineStatus;
            }
        }

        return UNKNOWN;
    }

    /**
     * @return The online status key
     */
    public @NotNull String getKey() {
        return this.key;
    }
}
