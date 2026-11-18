package cloud.thehsi.ComitasBotJ.Compatibility;

import org.jetbrains.annotations.NotNull;

/**
 * The kind of API change, decides which fields of a {@link Change} are used and how it is fixed.
 */
public enum ChangeKind {
    /**
     * A class was renamed or moved ({@code owner} -> {@code newName}, both fully qualified).
     */
    CLASS_RENAMED(Fix.AUTOMATIC),
    /**
     * A method was renamed ({@code owner}, {@code name}, {@code descriptor} -> {@code newName}).
     * Calls are renamed, so are overriding methods of plugin classes.
     */
    METHOD_RENAMED(Fix.AUTOMATIC),
    /**
     * A field or enum constant was renamed ({@code owner}, {@code name}, {@code descriptor} -> {@code newName}).
     */
    FIELD_RENAMED(Fix.AUTOMATIC),
    /**
     * A method was removed, or its signature or behaviour changed. Calls are redirected to a static {@code shim},
     * which takes the receiver (unless {@code static}) followed by the original arguments and returns the original type.
     */
    METHOD_REPLACED(Fix.SHIM),
    /**
     * A constructor was removed or changed. {@code new Owner(args)} is redirected to a static {@code shim},
     * which takes the original arguments and returns the owner type.
     */
    CONSTRUCTOR_REPLACED(Fix.SHIM),
    /**
     * A field was removed or changed. Reads are redirected to a static {@code shim}, which takes the receiver
     * (unless {@code static}) and returns the original field type. Writes can't be redirected.
     */
    FIELD_REPLACED(Fix.SHIM),
    /**
     * An abstract method, which plugins implement (e.g. {@code ConsoleCommandExecutor#execute}), got a new signature
     * ({@code descriptor} -> {@code newDescriptor}). Plugin classes implementing the old signature get a bridge method
     * with the new signature, which calls theirs. {@code arguments} lists, for every old parameter,
     * the index of the new parameter passed to it (added parameters are dropped).
     */
    ABSTRACT_METHOD_CHANGED(Fix.SHIM),
    /**
     * A change, which can't be fixed automatically (e.g. changed behaviour or a removed feature).
     * Plugins using {@code owner} (or only the member {@code name}, if set) get a warning.
     */
    NOTE(Fix.MANUAL);

    private final @NotNull Fix fix;

    ChangeKind(@NotNull Fix fix) {
        this.fix = fix;
    }

    @NotNull
    public Fix fix() {
        return fix;
    }

    public boolean isMethod() {
        return this == METHOD_RENAMED || this == METHOD_REPLACED;
    }

    public boolean isField() {
        return this == FIELD_RENAMED || this == FIELD_REPLACED;
    }

    /**
     * How a change is fixed for old plugins.
     */
    public enum Fix {
        /**
         * Fully fixed by rewriting names, both in plugin bytecode and in source code.
         */
        AUTOMATIC,
        /**
         * Fixed at runtime by the compatibility layer, the source has to be migrated by the developer.
         */
        SHIM,
        /**
         * Not fixed, the plugin may misbehave.
         */
        MANUAL
    }
}
