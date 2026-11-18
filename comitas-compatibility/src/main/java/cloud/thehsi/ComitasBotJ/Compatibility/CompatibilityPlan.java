package cloud.thehsi.ComitasBotJ.Compatibility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * The changelogs, which have to be applied (in order) to run a plugin built against {@code builtAgainst}
 * on {@code current}.
 */
public record CompatibilityPlan(@NotNull String builtAgainst, @NotNull String current, @NotNull List<ApiChangelog> steps) {
    public CompatibilityPlan {
        steps = List.copyOf(steps);
    }

    @NotNull
    @Unmodifiable
    public List<Change> changes() {
        return steps.stream().flatMap(step -> step.changes().stream()).toList();
    }

    /**
     * Maps an old class name through every class rename of the plan.
     *
     * @param internalName The class name, as used by the plugin
     * @return The class name in the current API
     */
    @NotNull
    public String mapClassName(@NotNull String internalName) {
        String name = internalName;
        for (ApiChangelog step : steps)
            for (Change change : step.changes())
                if (change.kind() == ChangeKind.CLASS_RENAMED && change.newName() != null) {
                    if (name.equals(change.owner())) name = change.newName();
                    else if (name.startsWith(change.owner() + "$"))
                        name = change.newName() + name.substring(change.owner().length());
                }
        return name;
    }
}
