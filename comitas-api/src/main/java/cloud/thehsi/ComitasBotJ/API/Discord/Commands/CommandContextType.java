package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public enum CommandContextType {
    GUILD("0"),
    BOT_DM("1"),
    PRIVATE_CHANNEL("2");

    private @NotNull
    final String key;

    CommandContextType(@NotNull String key) {
        this.key = key;
    }

    @NotNull
    public String getKey() {
        return this.key;
    }

}
