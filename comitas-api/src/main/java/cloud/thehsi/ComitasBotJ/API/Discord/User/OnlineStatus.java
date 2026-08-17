package cloud.thehsi.ComitasBotJ.API.Discord.User;

public enum OnlineStatus {
    ONLINE("online"),
    IDLE("idle"),
    DO_NOT_DISTURB("dnd"),
    INVISIBLE("invisible"),
    OFFLINE("offline"),
    UNKNOWN("");

    private final String key;

    OnlineStatus(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public static OnlineStatus fromKey(String key) {
        for(OnlineStatus onlineStatus : values()) {
            if (onlineStatus.key.equalsIgnoreCase(key)) {
                return onlineStatus;
            }
        }

        return UNKNOWN;
    }
}
