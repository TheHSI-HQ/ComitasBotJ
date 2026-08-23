package cloud.thehsi.ComitasBotJ.API.Discord.User.Presence;

import org.jetbrains.annotations.NotNull;

public enum ClientType {
    DESKTOP("desktop"),
    MOBILE("mobile"),
    WEB("web"),
    VR("vr"), //TODO: Not yet supported by JDA, put in as placeholder
    UNKNOWN("unknown");

    private @NotNull
    final String key;

    ClientType(@NotNull String key) {
        this.key = key;
    }

    public @NotNull
    static ClientType fromKey(@NotNull String key) {
        for(ClientType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }

        return UNKNOWN;
    }

    public @NotNull String getKey() {
        return this.key;
    }
}

