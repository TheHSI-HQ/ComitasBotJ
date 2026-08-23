package cloud.thehsi.ComitasBotJ.API.Discord.Message.Components;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public enum TimeStampFormat {
    RELATIVE("R"),
    SHORT_TIME("t"),
    LONG_TIME("T"),
    SHORT_DATE("d"),
    LONG_DATE("D"),
    LONG_DATE_WITH_SHORT_TIME("f"),
    LONG_DATE_WITH_DAY_OF_WEEK_AND_TIME("F");

    private @NotNull
    final String identifier;

    TimeStampFormat(@NotNull String identifier) {
        this.identifier = identifier;
    }

    @NotNull
    public String getIdentifier() {
        return identifier;
    }
}
