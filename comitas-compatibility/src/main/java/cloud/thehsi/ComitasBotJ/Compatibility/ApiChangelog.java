package cloud.thehsi.ComitasBotJ.Compatibility;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The machine readable changelog of one release: every breaking API change from {@code from} to {@code to}.
 * <p>
 * Published inside the {@code comitas-compatibility} jar as
 * {@code comitas-compatibility/changelog/<to>.json}, see {@code docs/COMPATIBILITY.md} for the format.
 *
 * @param from    The previous release
 * @param to      The release, which introduced the changes
 * @param changes The changes, in no particular order
 */
public record ApiChangelog(@NotNull String from, @NotNull String to, @NotNull List<Change> changes) {
    public static final int SCHEMA = 1;
    public static final String API_PACKAGE = "cloud/thehsi/ComitasBotJ/API/";

    private static final Gson GSON = new Gson();

    @NotNull
    public static ApiChangelog parse(@NotNull Reader reader, @NotNull String source) {
        Json json;
        try {
            json = GSON.fromJson(reader, Json.class);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException(source + ": invalid JSON: " + e.getMessage(), e);
        }
        if (json == null) throw new IllegalArgumentException(source + ": empty changelog");
        if (json.schema != SCHEMA)
            throw new IllegalArgumentException(source + ": unsupported schema " + json.schema + ", expected " + SCHEMA);
        if (json.from == null || json.to == null)
            throw new IllegalArgumentException(source + ": 'from' and 'to' are required");
        if (ApiVersion.compare(json.from, json.to) >= 0)
            throw new IllegalArgumentException(source + ": 'from' (" + json.from + ") must be older than 'to' (" + json.to + ")");

        List<Change> changes = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        if (json.changes != null) {
            for (JsonChange entry : json.changes) {
                Change change = entry.toChange(source);
                if (!ids.add(change.id())) throw new IllegalArgumentException(source + ": duplicate id " + change.id());
                changes.add(change);
            }
        }

        return new ApiChangelog(json.from, json.to, List.copyOf(changes));
    }

    @NotNull
    private static String internalName(@NotNull String className) {
        return className.replace('.', '/');
    }

    private static final class Json {
        int schema;
        String from;
        String to;
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        List<JsonChange> changes;
    }

    private static final class JsonChange {
        String id;
        ChangeKind kind;
        @SerializedName("class")
        String owner;
        String name;
        String descriptor;
        @SerializedName("static")
        boolean isStatic;
        String newName;
        String shim;
        String newDescriptor;
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        List<Integer> arguments;
        String description;
        String migration;
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        List<Map<String, Object>> rewrite;

        @NotNull
        Change toChange(@NotNull String source) {
            String where = getWhere(source);

            String member = name;
            if (kind == ChangeKind.CONSTRUCTOR_REPLACED) {
                if (member != null && !member.equals("<init>"))
                    throw new IllegalArgumentException(where + ": constructors have no name");
                member = "<init>";
            }

            String resolvedNewName = newName;
            String resolvedShim = null;

            switch (kind) {
                case CLASS_RENAMED -> {
                    if (member != null || descriptor != null)
                        throw new IllegalArgumentException(where + ": a class rename has no 'name' / 'descriptor'");
                    require(where, "newName", newName);
                    resolvedNewName = internalName(newName);
                }
                case METHOD_RENAMED, FIELD_RENAMED -> {
                    require(where, "name", member);
                    require(where, "descriptor", descriptor);
                    require(where, "newName", newName);
                }
                case METHOD_REPLACED, CONSTRUCTOR_REPLACED, FIELD_REPLACED -> {
                    require(where, "name", member);
                    require(where, "descriptor", descriptor);
                    require(where, "shim", shim);
                    int hash = shim.indexOf('#');
                    String shimOwner = getShimOwner(hash, where);
                    resolvedShim = shimOwner + shim.substring(hash);
                }
                case ABSTRACT_METHOD_CHANGED -> {
                    require(where, "name", member);
                    require(where, "descriptor", descriptor);
                    require(where, "newDescriptor", newDescriptor);
                    if (arguments == null) throw new IllegalArgumentException(where + ": 'arguments' is required");
                    validateDescriptor(where, newDescriptor, false);
                    validateDescriptor(where, descriptor, false);
                    Type oldType = Type.getMethodType(descriptor);
                    Type newType = Type.getMethodType(newDescriptor);
                    if (!oldType.getReturnType().equals(newType.getReturnType()))
                        throw new IllegalArgumentException(where + ": the return type can't change");
                    Type[] oldArgs = oldType.getArgumentTypes();
                    Type[] newArgs = newType.getArgumentTypes();
                    if (arguments.size() != oldArgs.length)
                        throw new IllegalArgumentException(where + ": 'arguments' needs one index per old parameter");
                    for (int i = 0; i < oldArgs.length; i++) {
                        Integer index = arguments.get(i);
                        if (index == null || index < 0 || index >= newArgs.length || !newArgs[index].equals(oldArgs[i]))
                            throw new IllegalArgumentException(where + ": argument " + i + " must be a new parameter of type "
                                    + oldArgs[i].getClassName());
                    }
                }
                case NOTE -> {
                    if (descriptor != null && member == null)
                        throw new IllegalArgumentException(where + ": a 'descriptor' needs a 'name'");
                }
            }

            if (descriptor != null) validateDescriptor(where, descriptor, kind.isField() || isFieldNote(descriptor));

            // Only API classes can change, plugin and JDK classes are never rewritten
            if (!internalName(owner).startsWith(API_PACKAGE))
                throw new IllegalArgumentException(where + ": 'class' must be in " + API_PACKAGE.replace('/', '.'));
            if (kind == ChangeKind.CLASS_RENAMED && !resolvedNewName.startsWith(API_PACKAGE))
                throw new IllegalArgumentException(where + ": 'newName' must be in " + API_PACKAGE.replace('/', '.'));

            boolean bridge = kind == ChangeKind.ABSTRACT_METHOD_CHANGED;
            return new Change(id, kind, internalName(owner), member, descriptor, isStatic, resolvedNewName, resolvedShim,
                    bridge ? newDescriptor : null, bridge ? List.copyOf(arguments) : null,
                    description, migration, rewrite == null ? null : List.copyOf(rewrite));
        }

        private @NotNull String getShimOwner(int hash, String where) {
            if (hash <= 0 || hash == shim.length() - 1)
                throw new IllegalArgumentException(where + ": 'shim' must look like Class#method");
            String shimClass = shim.substring(0, hash);
            // Short names are relative to the shim package
            String shimOwner = shimClass.indexOf('.') < 0 ? Change.SHIM_PACKAGE + shimClass : internalName(shimClass);
            if (!shimOwner.startsWith(Change.SHIM_PACKAGE) || shimOwner.indexOf('/', Change.SHIM_PACKAGE.length()) >= 0)
                throw new IllegalArgumentException(where + ": shims must be in " + Change.SHIM_PACKAGE.replace('/', '.'));
            return shimOwner;
        }

        private @NotNull String getWhere(@NotNull String source) {
            String where = source + (id == null ? "" : " (" + id + ")");
            if (id == null || id.isBlank()) throw new IllegalArgumentException(source + ": change without an id");
            if (kind == null) throw new IllegalArgumentException(where + ": unknown or missing 'kind'");
            if (owner == null) throw new IllegalArgumentException(where + ": 'class' is required");
            if (description == null || description.isBlank())
                throw new IllegalArgumentException(where + ": 'description' is required");
            return where;
        }

        private static boolean isFieldNote(@NotNull String descriptor) {
            return !descriptor.startsWith("(");
        }

        private static void require(@NotNull String where, @NotNull String field, @Nullable String value) {
            if (value == null || value.isBlank()) throw new IllegalArgumentException(where + ": '" + field + "' is required");
        }

        private static void validateDescriptor(@NotNull String where, @NotNull String descriptor, boolean field) {
            try {
                Type type = Type.getType(descriptor);
                if (field == (type.getSort() == Type.METHOD))
                    throw new IllegalArgumentException(where + ": '" + descriptor + "' is not a "
                            + (field ? "field" : "method") + " descriptor");
            } catch (RuntimeException e) {
                if (e instanceof IllegalArgumentException iae && iae.getMessage() != null && iae.getMessage().startsWith(where))
                    throw iae;
                throw new IllegalArgumentException(where + ": invalid descriptor '" + descriptor + "'", e);
            }
        }
    }
}
