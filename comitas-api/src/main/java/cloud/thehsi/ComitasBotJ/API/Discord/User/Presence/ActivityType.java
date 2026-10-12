package cloud.thehsi.ComitasBotJ.API.Discord.User.Presence;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * The type of an activity
 */
public enum ActivityType {
    /**
     * The playing activity type
     */
    PLAYING(0),
    /**
     * The streaming activity type
     */
    STREAMING(1),
    /**
     * The listening activity type
     */
    LISTENING(2),
    /**
     * The watching activity type
     */
    WATCHING(3),
    /**
     * The activity type for custom status activities
     */
    CUSTOM_STATUS(4),
    /**
     * The competing activity type
     */
    COMPETING(5);

    private final int key;

    ActivityType(int key) {
        this.key = key;
    }

    /**
     * Resolves an activity type key to an activity type.
     * If not resolvable, resolves to PLAYING
     *
     * @param key The activity type key
     * @return The resolved activity tyoe
     */
    @NotNull
    public static ActivityType fromKey(int key) {
        return switch (key) {
            case 1 -> STREAMING;
            case 2 -> LISTENING;
            case 3 -> WATCHING;
            case 4 -> CUSTOM_STATUS;
            case 5 -> COMPETING;
            default -> PLAYING;
        };
    }

    /**
     * Get the activity type key of this activity type
     *
     * @return The activity types key
     */
    @ApiStatus.Internal
    public int getKey() {
        return this.key;
    }
}
