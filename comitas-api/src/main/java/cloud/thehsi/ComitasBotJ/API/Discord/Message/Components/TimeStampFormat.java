package cloud.thehsi.ComitasBotJ.API.Discord.Message.Components;

import org.jetbrains.annotations.NotNull;

/**
 * An enum of Time Stamp Formats for timestamp components
 */
@SuppressWarnings("unused")
public enum TimeStampFormat {
    /**
     * Relative time (1 minute ago)
     */
    RELATIVE("R"),
    /**
     * Short time (1:23 AM)
     */
    SHORT_TIME("t"),
    /**
     * Long time (1:23:45 AM)
     */
    LONG_TIME("T"),
    /**
     * Short date (01/01/26)
     */
    SHORT_DATE("d"),
    /**
     * Long date (January 1, 2026)
     */
    LONG_DATE("D"),
    /**
     * Long date with short time (January 1, 2026 at 1:23 AM)
     */
    LONG_DATE_WITH_SHORT_TIME("f"),
    /**
     * Long date with day of week and time (Thursday, January 1, 2026 at 1:23 AM)
     */
    LONG_DATE_WITH_DAY_OF_WEEK_AND_TIME("F");

    private @NotNull
    final String identifier;

    TimeStampFormat(@NotNull String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return The TimeStampFormat identifier
     */
    @NotNull
    public String getIdentifier() {
        return identifier;
    }
}
