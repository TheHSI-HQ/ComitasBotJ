package cloud.thehsi.ComitasBotJ.Compatibility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects, which changes the compatibility layer applied to (or noticed in) a plugin.
 */
public final class CompatibilityReport {
    private final @NotNull Map<Change, Set<String>> hits = new ConcurrentHashMap<>();

    void hit(@NotNull Change change, @NotNull String className) {
        hits.computeIfAbsent(change, c -> ConcurrentHashMap.newKeySet()).add(className.replace('/', '.'));
    }

    public boolean isEmpty() {
        return hits.isEmpty();
    }

    /**
     * @return Every change, that affects the plugin, ordered by id
     */
    @NotNull
    @Unmodifiable
    public List<Change> changes() {
        return hits.keySet().stream().sorted(Comparator.comparing(Change::id)).toList();
    }

    @NotNull
    @Unmodifiable
    public List<Change> changes(@NotNull ChangeKind.Fix fix) {
        return changes().stream().filter(c -> c.kind().fix() == fix).toList();
    }

    /**
     * @return The plugin classes, which use the changed API
     */
    @NotNull
    @Unmodifiable
    public Set<String> classes(@NotNull Change change) {
        Set<String> classes = hits.get(change);
        return classes == null ? Set.of() : Set.copyOf(new TreeSet<>(classes));
    }
}
