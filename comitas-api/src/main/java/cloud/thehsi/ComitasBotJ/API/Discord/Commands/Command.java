package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describe a command, with a name and a description,
 * as well as an optional nsfw, commandType and commandContextType
 * <p>
 * Setting {@link #parent()} (and optionally {@link #group()}) turns the command into a subcommand,
 * <code>/parent name</code> or <code>/parent group name</code>. The parent has to be declared with
 * {@link CommandParent} (or a {@link cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandParentBuilder})
 * before the subcommand is registered.
 * <p>
 * Discord only knows nsfw, install types, contexts and permissions on the top level command, so for subcommands
 * {@link #nsfw()}, {@link #commandType()}, {@link #commandContextType()} and {@link #requiredPermissions()}
 * are taken from the parent and have no effect here.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@SuppressWarnings("unused")
public @interface Command {
    /**
     * The name of the command, or of the subcommand if {@link #parent()} is set
     *
     * @return The Command Name
     */
    String name();

    /**
     * @return The Command Description
     */
    String description();

    /**
     * @return Is the Command NSFW
     */
    boolean nsfw() default false;

    /**
     * @return The Command Types
     */
    CommandType[] commandType() default { CommandType.GUILD_INSTALL };

    /**
     * @return The Command Context Types
     */
    CommandContextType[] commandContextType() default { CommandContextType.GUILD };

    /**
     * The name of the parent command, if this command is a subcommand.
     * Leave empty for a regular top level command.
     * <p>
     * The parent has to be declared with {@link CommandParent} first.
     *
     * @return The name of the parent command, or empty if this isn't a subcommand
     */
    String parent() default "";

    /**
     * The name of the subcommand group this subcommand is part of (<code>/parent group name</code>),
     * leave empty to place the subcommand directly below the parent (<code>/parent name</code>).
     * <p>
     * Requires {@link #parent()}, and the group has to be declared in the parents {@link CommandParent#groups()}
     *
     * @return The name of the subcommand group, or empty for none
     */
    String group() default "";

    /**
     * Leave emoty for everyone
     *
     * @return The Command Required Permissions
     */
    Permission[] requiredPermissions() default {};
}

