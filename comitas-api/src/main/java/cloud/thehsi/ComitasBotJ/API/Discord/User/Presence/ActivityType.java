package cloud.thehsi.ComitasBotJ.API.Discord.User.Presence;

import org.jetbrains.annotations.NotNull;

public enum ActivityType {
    PLAYING(0),
    STREAMING(1),
    LISTENING(2),
    WATCHING(3),
    CUSTOM_STATUS(4),
    COMPETING(5);

    private final int key;

    ActivityType(int key) {
        this.key = key;
    }

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

    public int getKey() {
        return this.key;
    }
}
