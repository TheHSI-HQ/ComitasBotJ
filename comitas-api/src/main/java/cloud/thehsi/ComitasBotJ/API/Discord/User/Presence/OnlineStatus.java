package cloud.thehsi.ComitasBotJ.API.Discord.User.Presence;

import org.jetbrains.annotations.NotNull;

public enum OnlineStatus {
    ONLINE("online"),
    IDLE("idle"),
    DO_NOT_DISTURB("dnd"),
    INVISIBLE("invisible"),
    OFFLINE("offline"),
    UNKNOWN("");

    private @NotNull
    final String key;

    OnlineStatus(@NotNull String key) {
        this.key = key;
    }

    public @NotNull
    static OnlineStatus fromKey(@NotNull String key) {
        for(OnlineStatus onlineStatus : values()) {
            if (onlineStatus.key.equalsIgnoreCase(key)) {
                return onlineStatus;
            }
        }

        return UNKNOWN;
    }

    public @NotNull String getKey() {
        return this.key;
    }
}
