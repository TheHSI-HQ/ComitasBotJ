package cloud.thehsi.ComitasBotJ.API.Discord.User.Presence;

import org.jetbrains.annotations.NotNull;

/**
 * The type of a client
 */
public enum ClientType {
    /**
     * The desktop client type
     */
    DESKTOP("desktop"),
    /**
     * The mobile client type
     */
    MOBILE("mobile"),
    /**
     * The web client type
     */
    WEB("web"),
    /**
     * The vr client type
     * <p>
     * Not yet supported by JDA, put in as placeholder,
     * will be resolved as {@code UNKNOWN}
     *
     * @hidden
     */
    VR("vr"),
    /**
     * The unknown client type
     */
    UNKNOWN("unknown");

    private @NotNull
    final String key;

    ClientType(@NotNull String key) {
        this.key = key;
    }

    /**
     * Resolve a client type from a key
     *
     * @param key The client types key
     * @return The resolved client type or UNKNOWN
     */
    public @NotNull
    static ClientType fromKey(@NotNull String key) {
        for(ClientType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }

        return UNKNOWN;
    }

    /**
     * @return The client types key
     */
    public @NotNull String getKey() {
        return this.key;
    }
}

