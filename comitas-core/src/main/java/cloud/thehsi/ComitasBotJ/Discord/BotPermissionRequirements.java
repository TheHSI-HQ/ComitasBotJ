package cloud.thehsi.ComitasBotJ.Discord;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.API.Discord.RequiresBotPermission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads the {@link RequiresBotPermission} annotations of API methods, and applies the rules
 * the annotations don't list explicitly (implied {@link Permission#VIEW_CHANNEL},
 * {@link Permission#SEND_MESSAGES_IN_THREADS} in threads, {@link Permission#ADMINISTRATOR} granting everything).
 * <p>
 * Meant as the single source for checking calls against the bot's (or a plugin's) permissions.
 */
public final class BotPermissionRequirements {
    private static final @NotNull Map<Method, List<Requirement>> CACHE = new ConcurrentHashMap<>();

    private BotPermissionRequirements() {
    }

    /**
     * @param method An API method, e.g. {@code MessageChannel.class.getMethod("sendMessage", Component.class)}
     * @return The requirements of the method, empty if it doesn't need any permission
     */
    @NotNull
    @Unmodifiable
    public static List<Requirement> of(@NotNull Method method) {
        return CACHE.computeIfAbsent(method, m -> Arrays.stream(m.getAnnotationsByType(RequiresBotPermission.class))
                .map(a -> new Requirement(a.scope(), copy(a.value()), copy(a.anyOf()), copy(a.conditional())))
                .toList());
    }

    /**
     * @param method An API method
     * @return The index of the parameter {@link RequiresBotPermission.Scope#TARGET_CHANNEL} refers to, or -1 if it has none
     */
    public static int targetChannelParameter(@NotNull Method method) {
        Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++)
            if (Channel.class.isAssignableFrom(parameters[i]))
                return i;
        return -1;
    }

    @NotNull
    private static Set<Permission> copy(@NotNull Permission[] permissions) {
        Set<Permission> set = EnumSet.noneOf(Permission.class);
        set.addAll(Arrays.asList(permissions));
        return Collections.unmodifiableSet(set);
    }

    /**
     * One {@link RequiresBotPermission} annotation of a method
     *
     * @param scope       Where the permissions are needed
     * @param required    The always required permissions, as annotated
     * @param anyOf       The alternatives, of which one is always required, as annotated (empty if there are none)
     * @param conditional The permissions, which may be required, as annotated
     */
    public record Requirement(@NotNull RequiresBotPermission.Scope scope,
                              @NotNull @Unmodifiable Set<Permission> required,
                              @NotNull @Unmodifiable Set<Permission> anyOf,
                              @NotNull @Unmodifiable Set<Permission> conditional) {
        /**
         * @param thread Is the resolved channel a thread
         * @return The permissions to check before the call, including the implied ones
         */
        @NotNull
        public Set<Permission> requiredIn(boolean thread) {
            Set<Permission> set = effective(required, thread);
            if (scope != RequiresBotPermission.Scope.GUILD)
                set.add(Permission.VIEW_CHANNEL);
            return set;
        }

        /**
         * @param thread Is the resolved channel a thread
         * @return The alternatives, of which one is required before the call (empty if there are none)
         */
        @NotNull
        public Set<Permission> anyOfIn(boolean thread) {
            return effective(anyOf, thread);
        }

        /**
         * @param thread Is the resolved channel a thread
         * @return The permissions, which may be required, depending on the arguments or state
         */
        @NotNull
        public Set<Permission> conditionalIn(boolean thread) {
            return effective(conditional, thread);
        }

        /**
         * Can the call be made with the granted permissions, ignoring {@link #conditional()} ones
         *
         * @param granted The permissions the bot (or plugin) has in the resolved guild or channel
         * @param thread  Is the resolved channel a thread
         * @return Are all required permissions and one of the alternatives granted
         */
        public boolean satisfiedBy(@NotNull Set<Permission> granted, boolean thread) {
            if (granted.contains(Permission.ADMINISTRATOR))
                return true;
            if (!granted.containsAll(requiredIn(thread)))
                return false;
            Set<Permission> alternatives = anyOfIn(thread);
            return alternatives.isEmpty() || alternatives.stream().anyMatch(granted::contains);
        }

        /**
         * Is the call permitted for a plugin limited to the allowed permissions, without knowing the guild or channel.
         * <p>
         * {@link Permission#VIEW_CHANNEL} isn't checked, it is implied by every channel permission. As it isn't known
         * whether the call targets a thread, {@link Permission#SEND_MESSAGES} and {@link Permission#SEND_MESSAGES_IN_THREADS}
         * permit each other. {@link #conditional()} permissions are checked once they are needed, not here.
         *
         * @param allowed The permissions, the plugin was granted
         * @return Are all required permissions and one of the alternatives allowed
         */
        public boolean permittedBy(@NotNull Set<Permission> allowed) {
            return missingFrom(allowed).isEmpty();
        }

        /**
         * @param allowed The permissions, the plugin was granted
         * @return The permissions, which are needed but not allowed (all alternatives, if none of them is allowed),
         * see {@link #permittedBy(Set)}
         */
        @NotNull
        public Set<Permission> missingFrom(@NotNull Set<Permission> allowed) {
            Set<Permission> missing = EnumSet.noneOf(Permission.class);
            if (allowed.contains(Permission.ADMINISTRATOR))
                return missing;

            Set<Permission> effective = EnumSet.noneOf(Permission.class);
            effective.addAll(allowed);
            if (effective.contains(Permission.SEND_MESSAGES) || effective.contains(Permission.SEND_MESSAGES_IN_THREADS))
                effective.addAll(List.of(Permission.SEND_MESSAGES, Permission.SEND_MESSAGES_IN_THREADS));

            for (Permission permission : required)
                if (!effective.contains(permission))
                    missing.add(permission);
            if (!anyOf.isEmpty() && anyOf.stream().noneMatch(effective::contains))
                missing.addAll(anyOf);
            return missing;
        }

        @NotNull
        private static Set<Permission> effective(@NotNull Set<Permission> permissions, boolean thread) {
            Set<Permission> set = EnumSet.noneOf(Permission.class);
            set.addAll(permissions);
            if (thread && set.remove(Permission.SEND_MESSAGES))
                set.add(Permission.SEND_MESSAGES_IN_THREADS);
            return set;
        }
    }
}
