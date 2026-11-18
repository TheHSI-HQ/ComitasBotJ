package cloud.thehsi.ComitasBotJ.Compatibility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * All published {@link ApiChangelog}s, ordered from oldest to newest.
 * <p>
 * The changelogs are bundled as resources, {@value #INDEX} lists their file names.
 */
public final class ChangelogRepository {
    public static final String DIRECTORY = "comitas-compatibility/changelog/";
    public static final String INDEX = DIRECTORY + "index.txt";

    private static volatile @Nullable ChangelogRepository bundled;

    private final @NotNull List<ApiChangelog> changelogs;

    public ChangelogRepository(@NotNull List<ApiChangelog> changelogs) {
        List<ApiChangelog> sorted = new ArrayList<>(changelogs);
        sorted.sort(Comparator.comparingLong(c -> ApiVersion.id(c.to())));

        Set<String> ids = new HashSet<>();
        ApiChangelog previous = null;
        for (ApiChangelog changelog : sorted) {
            if (previous != null && ApiVersion.compare(changelog.from(), previous.to()) < 0)
                throw new IllegalArgumentException("Changelog " + changelog.from() + " -> " + changelog.to()
                        + " overlaps with " + previous.from() + " -> " + previous.to());
            for (Change change : changelog.changes())
                if (!ids.add(change.id()))
                    throw new IllegalArgumentException("Change id " + change.id() + " is used more than once");
            previous = changelog;
        }

        this.changelogs = List.copyOf(sorted);
    }

    /**
     * @return The changelogs bundled with this version of Comitas
     */
    @NotNull
    public static ChangelogRepository bundled() {
        ChangelogRepository repository = bundled;
        if (repository == null) {
            synchronized (ChangelogRepository.class) {
                repository = bundled;
                if (repository == null) bundled = repository = load(ChangelogRepository.class.getClassLoader());
            }
        }
        return repository;
    }

    @NotNull
    public static ChangelogRepository load(@NotNull ClassLoader loader) {
        List<ApiChangelog> changelogs = new ArrayList<>();
        for (String file : readIndex(loader)) {
            String path = DIRECTORY + file;
            try (InputStream in = loader.getResourceAsStream(path)) {
                if (in == null) throw new IllegalStateException("Changelog " + path + " is listed in the index, but missing");
                changelogs.add(ApiChangelog.parse(new InputStreamReader(in, StandardCharsets.UTF_8), path));
            } catch (IOException e) {
                throw new UncheckedIOException("Couldn't read " + path, e);
            }
        }
        return new ChangelogRepository(changelogs);
    }

    @NotNull
    static List<String> readIndex(@NotNull ClassLoader loader) {
        try (InputStream in = loader.getResourceAsStream(INDEX)) {
            if (in == null) throw new IllegalStateException("Missing " + INDEX);
            List<String> files = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.strip();
                    if (!line.isEmpty() && !line.startsWith("#")) files.add(line);
                }
            }
            return files;
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't read " + INDEX, e);
        }
    }

    @NotNull
    @Unmodifiable
    public List<ApiChangelog> changelogs() {
        return changelogs;
    }

    /**
     * @return The changelog of the given release, null if there is none
     */
    @Nullable
    public ApiChangelog release(@NotNull String version) {
        for (ApiChangelog changelog : changelogs)
            if (ApiVersion.compare(changelog.to(), version) == 0) return changelog;
        return null;
    }

    /**
     * @return The oldest API version, plugins can be upgraded from, null if there are no changelogs
     */
    @Nullable
    public String oldestSupported() {
        return changelogs.isEmpty() ? null : changelogs.getFirst().from();
    }

    /**
     * Finds the changes, which have to be applied to a plugin built against {@code builtAgainst}
     * to run on {@code current}.
     *
     * @return The plan, or null if the changelogs don't cover {@code builtAgainst}
     * (it's older than the first changelog or newer than {@code current})
     */
    @Nullable
    public CompatibilityPlan plan(@NotNull String builtAgainst, @NotNull String current) {
        String oldest = oldestSupported();
        if (oldest == null) return null;
        if (ApiVersion.compare(builtAgainst, oldest) < 0 || ApiVersion.compare(builtAgainst, current) >= 0) return null;

        List<ApiChangelog> steps = new ArrayList<>();
        for (ApiChangelog changelog : changelogs) {
            if (ApiVersion.compare(changelog.to(), builtAgainst) <= 0) continue;
            if (ApiVersion.compare(changelog.to(), current) > 0) break;
            steps.add(changelog);
        }
        return new CompatibilityPlan(builtAgainst, current, steps);
    }
}
