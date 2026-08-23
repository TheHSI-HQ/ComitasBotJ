package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public enum CommandType {
    GUILD_INSTALL("0"),
    USER_INSTALL("1");

    private @NotNull
    final String key;

    CommandType(@NotNull String key) {
        this.key = key;
    }

    @NotNull
    public String getKey() {
        return this.key;
    }
}
