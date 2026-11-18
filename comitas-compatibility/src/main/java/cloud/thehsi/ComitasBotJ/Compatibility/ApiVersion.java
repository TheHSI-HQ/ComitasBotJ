package cloud.thehsi.ComitasBotJ.Compatibility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Comitas API versions ({@code major.minor.patch}, optionally suffixed with {@code a} (alpha) or {@code b} (beta)).
 */
public final class ApiVersion {
    private ApiVersion() {
    }

    /**
     * Converts a version into a comparable number, alpha and beta versions are ordered before the release.
     *
     * @param version The version, e.g. {@code 0.0.17}
     * @return The comparable id, 0 if the version can't be parsed
     */
    public static long id(@NotNull String version) {
        version = version.trim().toLowerCase(Locale.ROOT);
        if (version.isEmpty()) return 0;

        int suffix = 0; // release
        char last = version.charAt(version.length() - 1);

        if (last == 'a') {
            suffix = -100;
            version = version.substring(0, version.length() - 1);
        } else if (last == 'b') {
            suffix = -99;
            version = version.substring(0, version.length() - 1);
        }

        String[] parts = version.split("\\.");

        int major = 0;
        int minor = 0;
        int patch = 0;

        try {
            if (parts.length > 0) major = Integer.parseInt(parts[0]);
            if (parts.length > 1) minor = Integer.parseInt(parts[1]);
            if (parts.length > 2) patch = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return 0;
        }

        return major * 1_000_000_000L
                + minor * 1_000_000L
                + patch * 1_000L
                + suffix;
    }

    public static int compare(@NotNull String a, @NotNull String b) {
        return Long.compare(id(a), id(b));
    }

    /**
     * A plugin's {@code api-target}, either a single version ({@code 0.0.16}) or an inclusive range ({@code 0.0.15-0.0.16}).
     *
     * @param lowest  The lowest supported version
     * @param highest The highest supported version, the API the plugin was built against
     */
    public record Target(@NotNull String lowest, @NotNull String highest) {
        @NotNull
        public static Target parse(@Nullable String target) {
            if (target == null || target.isBlank())
                throw new IllegalArgumentException("Plugin has no api-target");
            String[] parts = target.trim().split("-");
            if (parts.length == 1) return new Target(parts[0].trim(), parts[0].trim());
            if (parts.length == 2) return new Target(parts[0].trim(), parts[1].trim());
            throw new IllegalArgumentException("Invalid api-target: " + target);
        }

        public boolean supports(@NotNull String version) {
            long id = id(version);
            return id(lowest) <= id && id <= id(highest);
        }

        @Override
        @NotNull
        public String toString() {
            return lowest.equals(highest) ? lowest : lowest + "-" + highest;
        }
    }
}
