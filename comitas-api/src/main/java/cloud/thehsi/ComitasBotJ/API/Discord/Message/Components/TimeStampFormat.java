package cloud.thehsi.ComitasBotJ.API.Discord.Message.Components;

@SuppressWarnings("unused")
public enum TimeStampFormat {
    RELATIVE("R"),
    SHORT_TIME("t"),
    LONG_TIME("T"),
    SHORT_DATE("d"),
    LONG_DATE("D"),
    LONG_DATE_WITH_SHORT_TIME("f"),
    LONG_DATE_WITH_DAY_OF_WEEK_AND_TIME("F");

    private final String identifier;

    TimeStampFormat(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
