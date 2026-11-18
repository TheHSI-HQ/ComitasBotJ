package cloud.thehsi.ComitasBotJ.API.Discord;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the Discord permissions the bot needs, for an API method to succeed.
 * <p>
 * Methods without this annotation don't need any Discord permission, apart from being able to see
 * the involved guild or channel (e.g. getters, which read cached data, or interaction replies).
 * <p>
 * A method can carry the annotation multiple times, if it needs permissions in different scopes
 * (e.g. {@link cloud.thehsi.ComitasBotJ.API.Discord.User.Member#moveTo} needs {@link Permission#MOVE_MEMBERS} in the
 * current and in the target channel), or multiple groups of {@link #anyOf()} alternatives. All annotations must be satisfied.
 * <p>
 * Rules, which apply to every requirement and are therefore not listed explicitly:
 * <ul>
 *     <li>{@link Scope#CHANNEL} and {@link Scope#TARGET_CHANNEL} requirements imply {@link Permission#VIEW_CHANNEL}</li>
 *     <li>In thread channels Discord checks {@link Permission#SEND_MESSAGES_IN_THREADS} instead of {@link Permission#SEND_MESSAGES}</li>
 *     <li>Outside of guilds (e.g. direct messages) no permission is required, the requirement is skipped</li>
 *     <li>{@link Permission#ADMINISTRATOR} grants every permission</li>
 * </ul>
 * The annotation documents what Discord requires, it doesn't check anything by itself.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(RequiresBotPermission.List.class)
public @interface RequiresBotPermission {
    /**
     * The permissions, which are always required. A call can be rejected up front, if one is missing.
     *
     * @return The always required permissions
     */
    Permission[] value() default {};

    /**
     * Alternative permissions, of which at least one is always required (e.g. deleting an invite requires
     * {@link Permission#MANAGE_CHANNEL} or {@link Permission#MANAGE_GUILD}). A call can be rejected up front,
     * if all of them are missing. Repeat the annotation, if a method needs multiple groups of alternatives.
     * Empty, if there are no alternatives.
     *
     * @return The alternative permissions
     */
    Permission[] anyOf() default {};

    /**
     * The permissions, which are only required depending on the arguments or state
     * (e.g. {@link Permission#EMBED_LINKS} when a message contains embeds, or {@link Permission#MANAGE_MESSAGES}
     * when deleting someone else's message). Calls must not be rejected up front because of them,
     * the implementation checks them once it knows they are needed.
     *
     * @return The permissions, which may be required
     */
    Permission[] conditional() default {};

    /**
     * Where the permissions are needed.
     *
     * @return The scope of the permissions
     */
    Scope scope() default Scope.CHANNEL;

    /**
     * Where a permission is checked
     */
    enum Scope {
        /**
         * The guild wide permission in the guild the object belongs to
         * (e.g. the guild itself, the guild of a member or of a ban)
         */
        GUILD,
        /**
         * The permission in the channel the object is or belongs to
         * (e.g. the channel itself, the channel of a message, reaction or invite, the voice channel of a member)
         */
        CHANNEL,
        /**
         * The permission in the channel passed as an argument (the first parameter of a
         * {@link cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel} type)
         */
        TARGET_CHANNEL
    }

    /**
     * The container of repeated {@link RequiresBotPermission} annotations
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface List {
        /**
         * @return The repeated annotations
         */
        RequiresBotPermission[] value();
    }
}
