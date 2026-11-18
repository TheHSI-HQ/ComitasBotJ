package cloud.thehsi.ComitasBotJ.Compatibility.Tools;

import cloud.thehsi.ComitasBotJ.Compatibility.ApiChangelog;
import cloud.thehsi.ComitasBotJ.Compatibility.Change;
import cloud.thehsi.ComitasBotJ.Compatibility.ChangeKind;
import cloud.thehsi.ComitasBotJ.Compatibility.ChangelogRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Generates OpenRewrite recipes, which upgrade plugin source code, from the changelogs.
 * <p>
 * Every release gets a recipe {@code cloud.thehsi.ComitasBotJ.UpgradeTo_<version>}, which first runs the recipe of the
 * previous release, so upgrading from any older version is a single recipe.
 * {@code cloud.thehsi.ComitasBotJ.UpgradeToLatest} upgrades to the newest release.
 * <p>
 * Renames are applied automatically. Calls, which need a developer, get a {@code TODO Comitas <version>} comment with the
 * migration hint, constructors and fields are marked with OpenRewrite search markers ({@code /*~~>*}{@code /}).
 * A change can provide its own {@code rewrite} recipes instead.
 */
public final class RewriteRecipeGenerator {
    public static final String RECIPE_PREFIX = "cloud.thehsi.ComitasBotJ.UpgradeTo_";
    public static final String LATEST = "cloud.thehsi.ComitasBotJ.UpgradeToLatest";

    private RewriteRecipeGenerator() {
    }

    @NotNull
    public static String recipeName(@NotNull String version) {
        return RECIPE_PREFIX + version.replaceAll("[^A-Za-z0-9]", "_");
    }

    @NotNull
    public static String generate(@NotNull ChangelogRepository repository) {
        StringBuilder yaml = new StringBuilder("# Generated from the Comitas API changelogs, don't edit\n");
        String previous = null;
        for (ApiChangelog changelog : repository.changelogs()) {
            List<Object> recipes = new ArrayList<>();
            if (previous != null) recipes.add(previous);
            for (Change change : changelog.changes())
                recipes.addAll(recipesFor(change, changelog.to()));
            recipes.add(Map.of("org.openrewrite.maven.UpgradeDependencyVersion", ordered(
                    "groupId", "cloud.thehsi.ComitasBotJ",
                    "artifactId", "comitas-api",
                    "newVersion", changelog.to())));

            String name = recipeName(changelog.to());
            appendRecipe(yaml, name, "Upgrade a Comitas plugin to API " + changelog.to(),
                    "Migrates a Comitas plugin from API " + changelog.from() + " (or older) to " + changelog.to()
                            + ". Members, which need manual work, are marked.", recipes);
            previous = name;
        }

        if (previous != null)
            appendRecipe(yaml, LATEST, "Upgrade a Comitas plugin to the latest API",
                    "Migrates a Comitas plugin to the newest Comitas API. Members, which need manual work, are marked.",
                    List.of(previous));
        return yaml.toString();
    }

    @NotNull
    static List<Object> recipesFor(@NotNull Change change, @NotNull String version) {
        if (change.rewrite() != null) return new ArrayList<>(change.rewrite());
        // The compiler reports implementations of the old signature, they have to be migrated by hand
        if (change.kind() == ChangeKind.ABSTRACT_METHOD_CHANGED) return new ArrayList<>();

        String type = typeName(change.owner());
        String name = change.name();
        String descriptor = change.descriptor();
        Object recipe = switch (change.kind()) {
            case CLASS_RENAMED -> {
                assert change.newName() != null;
                yield Map.of("org.openrewrite.java.ChangeType", ordered(
                        "oldFullyQualifiedTypeName", type,
                        "newFullyQualifiedTypeName", typeName(change.newName())));
            }
            case METHOD_RENAMED -> {
                assert name != null;
                yield Map.of("org.openrewrite.java.ChangeMethodName", ordered(
                        "methodPattern", methodPattern(change.owner(), name, descriptor),
                        "newMethodName", change.newName(),
                        "matchOverrides", true));
            }
            case FIELD_RENAMED -> {
                if (change.isStatic()) {
                    yield Map.of("org.openrewrite.java.ReplaceConstantWithAnotherConstant", ordered(
                    "existingFullyQualifiedConstantName", type + "." + name,
                    "fullyQualifiedConstantName", type + "." + change.newName()));
                } else {
                    assert name != null;
                    yield findField(type, name);
                }
            }
            case METHOD_REPLACED -> commentMethods(change, version);
            case CONSTRUCTOR_REPLACED -> {
                assert name != null;
                yield findMethods(change.owner(), name, descriptor);
            }
            case FIELD_REPLACED -> {
                assert name != null;
                yield findField(type, name);
            }
            case ABSTRACT_METHOD_CHANGED -> throw new IllegalStateException("handled above");
            case NOTE -> {
                if (name == null) yield Map.of("org.openrewrite.java.search.FindTypes", ordered(
                        "fullyQualifiedTypeName", type));
                if (descriptor != null && !descriptor.startsWith("(")) yield findField(type, name);
                if (name.equals("<init>")) yield findMethods(change.owner(), name, descriptor);
                yield commentMethods(change, version);
            }
        };
        return List.of(recipe);
    }

    /**
     * Calls, which need manual work, get a {@code /* TODO Comitas <version>: <migration> *}{@code /} comment.
     */
    @NotNull
    private static Map<String, Object> commentMethods(@NotNull Change change, @NotNull String version) {
        String text = change.migration() != null ? change.migration() : change.description();
        String comment = ("TODO Comitas " + version + ": " + text).replace("*/", "* /").replace('\n', ' ');
        return Map.of("org.openrewrite.java.AddCommentToMethodInvocations", ordered(
                "methodPattern", methodPattern(change.owner(), Objects.requireNonNull(change.name()), change.descriptor()),
                "comment", comment));
    }

    @NotNull
    private static Map<String, Object> findMethods(@NotNull String owner, @NotNull String name, @Nullable String descriptor) {
        return Map.of("org.openrewrite.java.search.FindMethods", ordered(
                "methodPattern", methodPattern(owner, name, descriptor),
                "matchOverrides", true));
    }

    @NotNull
    private static Map<String, Object> findField(@NotNull String type, @NotNull String name) {
        return Map.of("org.openrewrite.java.search.FindFields", ordered(
                "fullyQualifiedTypeName", type,
                "fieldName", name));
    }

    /**
     * OpenRewrite method pattern, e.g. {@code a.b.Guild getChannelById(java.lang.String)}.
     */
    @NotNull
    static String methodPattern(@NotNull String owner, @NotNull String name, @Nullable String descriptor) {
        String method = name.equals("<init>") ? "<constructor>" : name;
        if (descriptor == null) return typeName(owner) + " " + method + "(..)";
        List<String> args = new ArrayList<>();
        for (Type arg : Type.getArgumentTypes(descriptor))
            args.add(arg.getClassName());
        return typeName(owner) + " " + method + "(" + String.join(", ", args) + ")";
    }

    @NotNull
    private static String typeName(@NotNull String internalName) {
        return internalName.replace('/', '.');
    }

    @NotNull
    private static Map<String, Object> ordered(Object... keysAndValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2)
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        return map;
    }

    private static void appendRecipe(@NotNull StringBuilder yaml, @NotNull String name, @NotNull String displayName,
                                     @NotNull String description, @NotNull List<Object> recipes) {
        yaml.append("---\n");
        yaml.append("type: specs.openrewrite.org/v1beta/recipe\n");
        yaml.append("name: ").append(name).append('\n');
        yaml.append("displayName: ").append(quote(displayName)).append('\n');
        yaml.append("description: ").append(quote(description)).append('\n');
        yaml.append("recipeList:\n");
        for (Object recipe : recipes) {
            if (recipe instanceof String recipeName) {
                yaml.append("  - ").append(recipeName).append('\n');
                continue;
            }
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) recipe).entrySet()) {
                yaml.append("  - ").append(entry.getKey()).append(":\n");
                for (Map.Entry<?, ?> option : ((Map<?, ?>) entry.getValue()).entrySet())
                    yaml.append("      ").append(option.getKey()).append(": ").append(scalar(option.getValue())).append('\n');
            }
        }
    }

    @NotNull
    private static String scalar(Object value) {
        if (value instanceof Boolean || value instanceof Number) return value.toString();
        return quote(String.valueOf(value));
    }

    @NotNull
    private static String quote(@NotNull String value) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) out.append(String.format("\\x%02x", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.append('"').toString();
    }
}
