package cloud.thehsi.ComitasBotJ.Compatibility;

import org.jetbrains.annotations.NotNull;

/**
 * Answers subtype questions for the compatibility layer, e.g. whether a call on {@code TextChannel}
 * reaches a changed method declared in {@code Channel}.
 */
@FunctionalInterface
public interface TypeResolver {
    /**
     * @param type      Internal name of a class (plugin or host class)
     * @param supertype Internal name of a host class
     * @return Whether {@code type} is a strict subtype of {@code supertype}
     */
    boolean isSubtypeOf(@NotNull String type, @NotNull String supertype);

    /**
     * Resolves host classes through the given class loader, without initializing them.
     * Unknown classes (e.g. plugin classes or classes removed from the API) are subtypes of nothing.
     */
    @NotNull
    static TypeResolver host(@NotNull ClassLoader loader) {
        return (type, supertype) -> {
            if (type.equals(supertype) || type.startsWith("[")) return false;
            try {
                Class<?> sub = Class.forName(type.replace('/', '.'), false, loader);
                Class<?> sup = Class.forName(supertype.replace('/', '.'), false, loader);
                return sup.isAssignableFrom(sub);
            } catch (ClassNotFoundException | LinkageError e) {
                return false;
            }
        };
    }
}
