package cloud.thehsi.ComitasBotJ.Compatibility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Map;

/**
 * One entry of an {@link ApiChangelog}.
 * <p>
 * Class names are internal names ({@code cloud/thehsi/...}), descriptors are JVM descriptors.
 * Every member refers to the API as it was before the change (the changelog's {@code from} version).
 *
 * @param id          Unique (across all changelogs) id, used in warnings and recipes
 * @param kind        The kind of change
 * @param owner       The (old) class
 * @param name        The (old) member name, {@code <init>} for constructors, null for whole classes
 * @param descriptor  The (old) member descriptor, null to match every overload (only for {@link ChangeKind#NOTE})
 * @param isStatic    Whether the member is static
 * @param newName     The new member name, or for {@link ChangeKind#CLASS_RENAMED} the new internal class name
 * @param shim        The shim method (internal class name + "#" + method name), for the *_REPLACED kinds
 * @param newDescriptor For {@link ChangeKind#ABSTRACT_METHOD_CHANGED}: the new method descriptor
 * @param arguments   For {@link ChangeKind#ABSTRACT_METHOD_CHANGED}: per old parameter, the index of the new parameter
 * @param description What changed
 * @param migration   How developers migrate their source code, null if done automatically
 * @param rewrite     OpenRewrite recipe entries, replacing the generated ones
 */
public record Change(
        @NotNull String id,
        @NotNull ChangeKind kind,
        @NotNull String owner,
        @Nullable String name,
        @Nullable String descriptor,
        boolean isStatic,
        @Nullable String newName,
        @Nullable String shim,
        @Nullable String newDescriptor,
        @Nullable List<Integer> arguments,
        @NotNull String description,
        @Nullable String migration,
        @Nullable List<Map<String, Object>> rewrite
) {
    /**
     * Shims may only live in this package, it is the only part of the compatibility layer plugins can reach.
     */
    public static final String SHIM_PACKAGE = "cloud/thehsi/ComitasBotJ/Compatibility/Shims/";

    @NotNull
    public String shimOwner() {
        if (shim == null) throw new IllegalStateException(id + " has no shim");
        return shim.substring(0, shim.indexOf('#'));
    }

    @NotNull
    public String shimName() {
        if (shim == null) throw new IllegalStateException(id + " has no shim");
        return shim.substring(shim.indexOf('#') + 1);
    }

    /**
     * The descriptor the shim must have, in terms of the old API.
     * The compatibility layer maps class renames of later changes onto it.
     */
    @NotNull
    public String shimDescriptor() {
        if (descriptor == null) throw new IllegalStateException(id + " has no descriptor");
        Type ownerType = Type.getObjectType(owner);
        return switch (kind) {
            case METHOD_REPLACED -> {
                Type method = Type.getMethodType(descriptor);
                if (isStatic) yield descriptor;
                Type[] args = new Type[method.getArgumentTypes().length + 1];
                args[0] = ownerType;
                System.arraycopy(method.getArgumentTypes(), 0, args, 1, args.length - 1);
                yield Type.getMethodDescriptor(method.getReturnType(), args);
            }
            case CONSTRUCTOR_REPLACED -> Type.getMethodDescriptor(ownerType, Type.getMethodType(descriptor).getArgumentTypes());
            case FIELD_REPLACED -> isStatic
                    ? Type.getMethodDescriptor(Type.getType(descriptor))
                    : Type.getMethodDescriptor(Type.getType(descriptor), ownerType);
            default -> throw new IllegalStateException(id + " (" + kind + ") has no shim");
        };
    }

    /**
     * Whether this change is about the given member (or the whole class, for class level changes).
     */
    public boolean matchesMember(@NotNull String memberName, @NotNull String memberDescriptor) {
        if (name == null) return true;
        return name.equals(memberName) && (descriptor == null || descriptor.equals(memberDescriptor));
    }

    @NotNull
    public String displayName() {
        String type = owner.replace('/', '.');
        if (name == null) return type;
        return type + "#" + name + (descriptor == null ? "" : descriptor);
    }
}
