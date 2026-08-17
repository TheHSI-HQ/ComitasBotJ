package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

@SuppressWarnings("unused")
public enum CommandContextType {
    GUILD("0"),
    BOT_DM("1"),
    PRIVATE_CHANNEL("2");

    private final String key;
    CommandContextType(String key) {
        this.key = key;
    }
    public String getKey() {
        return this.key;
    }

}
