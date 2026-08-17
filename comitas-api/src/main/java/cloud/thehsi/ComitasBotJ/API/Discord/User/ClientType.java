package cloud.thehsi.ComitasBotJ.API.Discord.User;

public enum ClientType {
    DESKTOP("desktop"),
    MOBILE("mobile"),
    WEB("web"),
    VR("vr"), //TODO: Not yet supported by JDA, put in as placeholder
    UNKNOWN("unknown");

    private final String key;

    ClientType(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public static ClientType fromKey(String key) {
        for(ClientType type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }

        return UNKNOWN;
    }
}

