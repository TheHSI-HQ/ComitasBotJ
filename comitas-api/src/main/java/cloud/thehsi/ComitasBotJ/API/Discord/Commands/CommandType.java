package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

@SuppressWarnings("unused")
public enum CommandType {
    GUILD_INSTALL("0"),
    USER_INSTALL("1");

    private final String key;
    CommandType(String key) {
        this.key = key;
    }
    public String getKey() {
        return this.key;
    }
}
