package cloud.thehsi.ComitasBotJ.Compatibility.Tools;

import cloud.thehsi.ComitasBotJ.Compatibility.ApiChangelog;
import cloud.thehsi.ComitasBotJ.Compatibility.ApiVersion;
import cloud.thehsi.ComitasBotJ.Compatibility.Change;
import cloud.thehsi.ComitasBotJ.Compatibility.ChangeKind;
import cloud.thehsi.ComitasBotJ.Compatibility.ChangelogRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Finds binary incompatible changes (removed classes, methods, fields and changed signatures)
 * between two API versions, and checks that the changelog covers each of them.
 */
public final class ApiDiff {
    /**
     * API classes and members plugins can't use (blocked by the plugin sandbox), changes to them don't break plugins.
     */
    private static final Set<String> INTERNAL = Set.of(
            "cloud/thehsi/ComitasBotJ/API/Bot/InternalComitasImpl",
            "cloud/thehsi/ComitasBotJ/API/Bot/UtilityBackend",
            "cloud/thehsi/ComitasBotJ/API/Bot/Comitas.getUtilityBackend",
            "cloud/thehsi/ComitasBotJ/API/Bot/Comitas.init"
    );

    private ApiDiff() {
    }

    /**
     * A removed / changed part of the API.
     *
     * @param owner      Internal class name
     * @param name       Member name, null if the whole class was removed
     * @param descriptor Member descriptor
     * @param field      Whether the member is a field
     */
    public record Break(@NotNull String owner, @Nullable String name, @Nullable String descriptor, boolean field) {
        @NotNull
        public String displayName() {
            String type = owner.replace('/', '.');
            if (name == null) return "class " + type;
            return (field ? "field " : name.equals("<init>") ? "constructor " : "method ") + type + "#" + name
                    + (field ? " : " : "") + descriptor;
        }
    }

    @NotNull
    public static List<Break> breaks(@NotNull ApiSurface old, @NotNull ApiSurface current) {
        List<Break> breaks = new ArrayList<>();
        for (Map.Entry<String, ApiSurface.ClassInfo> entry : old.classes().entrySet()) {
            String owner = entry.getKey();
            if (INTERNAL.contains(owner)) continue;
            ApiSurface.ClassInfo info = entry.getValue();

            if (current.get(owner) == null) {
                breaks.add(new Break(owner, null, null, false));
                continue;
            }

            for (String method : info.methods()) {
                String name = method.substring(0, method.indexOf('('));
                if (INTERNAL.contains(owner + "." + name)) continue;
                if (!current.hasMember(owner, method, false))
                    breaks.add(new Break(owner, name, method.substring(name.length()), false));
            }
            for (String field : info.fields()) {
                String name = field.substring(0, field.indexOf(':'));
                if (INTERNAL.contains(owner + "." + name)) continue;
                if (!current.hasMember(owner, field, true))
                    breaks.add(new Break(owner, name, field.substring(name.length() + 1), true));
            }
        }
        return breaks;
    }

    /**
     * @return The changes of all changelogs after {@code baseline}, up to (including) {@code current}
     */
    @NotNull
    public static List<Change> changesSince(@NotNull ChangelogRepository repository, @NotNull String baseline,
                                            @NotNull String current) {
        List<Change> changes = new ArrayList<>();
        for (ApiChangelog changelog : repository.changelogs())
            if (ApiVersion.compare(changelog.to(), baseline) > 0 && ApiVersion.compare(changelog.to(), current) <= 0)
                changes.addAll(changelog.changes());
        return changes;
    }

    /**
     * @return The breaks, which no change of the changelog covers
     */
    @NotNull
    public static List<Break> uncovered(@NotNull List<Break> breaks, @NotNull List<Change> changes) {
        return breaks.stream().filter(b -> changes.stream().noneMatch(c -> covers(c, b))).toList();
    }

    static boolean covers(@NotNull Change change, @NotNull Break b) {
        boolean classLevel = change.name() == null;
        if (classLevel && (change.kind() == ChangeKind.CLASS_RENAMED || change.kind() == ChangeKind.NOTE))
            return b.owner().equals(change.owner()) || b.owner().startsWith(change.owner() + "$");
        if (!change.owner().equals(b.owner()) || b.name() == null || classLevel) return false;
        if (!change.name().equals(b.name())) return false;
        if (change.descriptor() != null && !change.descriptor().equals(b.descriptor())) return false;

        return switch (change.kind()) {
            case METHOD_RENAMED, METHOD_REPLACED, ABSTRACT_METHOD_CHANGED -> !b.field() && !b.name().equals("<init>");
            case CONSTRUCTOR_REPLACED -> !b.field() && b.name().equals("<init>");
            case FIELD_RENAMED, FIELD_REPLACED -> b.field();
            case NOTE -> true;
            case CLASS_RENAMED -> false;
        };
    }

    /**
     * A changelog entry skeleton for an uncovered break, to be completed by the developer.
     */
    @NotNull
    public static String suggestion(@NotNull Break b, @NotNull ApiSurface current) {
        String type = b.owner().replace('/', '.');
        String id = (b.owner().substring(b.owner().lastIndexOf('/') + 1) + (b.name() == null ? "" : "-" + b.name()))
                .replaceAll("[^A-Za-z0-9]+", "-").replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();

        StringBuilder json = new StringBuilder("    {\n");
        json.append("      \"id\": \"").append(id).append("\",\n");
        if (b.name() == null) {
            json.append("      \"kind\": \"CLASS_RENAMED or NOTE\",\n");
            json.append("      \"class\": \"").append(type).append("\",\n");
            json.append("      \"newName\": \"(for CLASS_RENAMED) the new fully qualified name\",\n");
        } else {
            String kind = b.field() ? "FIELD_RENAMED / FIELD_REPLACED / NOTE"
                    : b.name().equals("<init>") ? "CONSTRUCTOR_REPLACED / NOTE" : "METHOD_RENAMED / METHOD_REPLACED / NOTE";
            json.append("      \"kind\": \"").append(kind).append("\",\n");
            json.append("      \"class\": \"").append(type).append("\",\n");
            if (!b.name().equals("<init>")) json.append("      \"name\": \"").append(b.name()).append("\",\n");
            json.append("      \"descriptor\": \"").append(b.descriptor()).append("\",\n");
            ApiSurface.ClassInfo info = current.get(b.owner());
            if (info != null && !b.field()) {
                List<String> overloads = info.methods().stream().filter(m -> m.startsWith(b.name() + "(")).toList();
                if (!overloads.isEmpty())
                    json.append("      \"_now\": \"").append(String.join(", ", overloads)).append("\",\n");
            }
            json.append("      \"shim\": \"(for *_REPLACED) V").append("x_y_z#method\",\n");
        }
        json.append("      \"description\": \"What changed\",\n");
        json.append("      \"migration\": \"How to update plugin source code\"\n");
        json.append("    }");
        return json.toString();
    }
}
