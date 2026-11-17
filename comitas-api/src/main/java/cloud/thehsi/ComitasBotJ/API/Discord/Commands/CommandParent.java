package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a parent command, the top level command <code>/name</code> that holds subcommands
 * (<code>/name subcommand</code>) and subcommand groups (<code>/name group subcommand</code>).
 * <p>
 * Place it on a {@link CommandSupplier} class, the parent is declared once the supplier gets registered.
 * The subcommands themselves are normal {@link Command} methods with {@link Command#parent()} set,
 * they may live in the same supplier or in any other one registered afterward.
 * <p>
 * A parent command can't be run itself, Discord only allows running its subcommands.
 * Declaring the same parent multiple times is fine, the first declaration wins, but additional groups are merged in.
 * <p>
 * Discord limits a command to 25 subcommands and 25 groups, and every group to 25 subcommands.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(CommandParents.class)
@SuppressWarnings("unused")
public @interface CommandParent {
    /**
     * Must be lowercase, and not used by any other command
     *
     * @return The Command Name
     */
    String name();

    /**
     * @return The Command Description
     */
    String description();

    /**
     * @return Is the Command, and with it all subcommands, NSFW
     */
    boolean nsfw() default false;

    /**
     * @return The Command Types, applies to all subcommands
     */
    CommandType[] commandType() default { CommandType.GUILD_INSTALL };

    /**
     * @return The Command Context Types, applies to all subcommands
     */
    CommandContextType[] commandContextType() default { CommandContextType.GUILD };

    /**
     * Leave empty for everyone
     *
     * @return The Command Required Permissions, applies to all subcommands
     */
    Permission[] requiredPermissions() default {};

    /**
     * @return The subcommand groups of this command
     */
    CommandGroup[] groups() default {};
}
