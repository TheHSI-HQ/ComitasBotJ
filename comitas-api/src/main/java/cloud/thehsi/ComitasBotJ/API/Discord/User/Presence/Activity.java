package cloud.thehsi.ComitasBotJ.API.Discord.User.Presence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("ClassCanBeRecord")
public class Activity {
    final @NotNull String name;
    final @Nullable String url;
    final @NotNull ActivityType type;

    private Activity(@NotNull String name, @Nullable String url, @NotNull ActivityType type) {
        this.name = name;
        this.url = url;
        this.type = type;
    }

    private static boolean isValidStreamingUrl(@Nullable String url) {
        return url != null && Pattern.compile(
                "https?://(www\\.)?(twitch\\.tv/|youtube\\.com/watch\\?v=).+",
                Pattern.CASE_INSENSITIVE
        ).matcher(url).matches();
    }

    @NotNull
    public static Activity playing(@NotNull String name) {
        name = name.trim();
        return new Activity(name, null, ActivityType.PLAYING);
    }

    @NotNull
    public static Activity streaming(@NotNull String name, @Nullable String url) {
        name = name.isBlank() ? name : name.trim();
        ActivityType type;
        if (isValidStreamingUrl(url)) {
            type = ActivityType.STREAMING;
        } else {
            type = ActivityType.PLAYING;
        }

        return new Activity(name, url, type);
    }

    @NotNull
    public static Activity listening(@NotNull String name) {
        name = name.trim();
        return new Activity(name, null, ActivityType.LISTENING);
    }

    @NotNull
    public static Activity watching(@NotNull String name) {
        name = name.trim();
        return new Activity(name, null, ActivityType.WATCHING);
    }

    @NotNull
    public static Activity competing(@NotNull String name) {
        name = name.trim();
        return new Activity(name, null, ActivityType.COMPETING);
    }

    @NotNull
    public static Activity customStatus(@NotNull String name) {
        name = name.trim();
        return new Activity(name, null, ActivityType.CUSTOM_STATUS);
    }

    @NotNull
    public static Activity of(@NotNull ActivityType type, @NotNull String name) {
        return of(type, name, null);
    }

    @NotNull
    @SuppressWarnings("SameParameterValue")
    public static Activity of(@NotNull ActivityType type, @NotNull String name, @Nullable String url) {
        return switch (type.ordinal()) {
            case 0 -> playing(name);
            case 1 -> streaming(name, url);
            case 2 -> listening(name);
            case 3 -> watching(name);
            case 4 -> customStatus(name);
            case 5 -> competing(name);
            default -> throw new IllegalArgumentException("ActivityType " + type + " is not supported!");
        };
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @NotNull
    public ActivityType getType() {
        return type;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object == null || getClass() != object.getClass()) return false;

        Activity activity = (Activity) object;
        return name.equals(activity.name) && Objects.equals(url, activity.url) && type == activity.type;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + Objects.hashCode(url);
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    @NotNull
    public String toString() {
        return "Activity{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", type=" + type +
                '}';
    }
}
