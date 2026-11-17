package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a subcommand group (<code>/parent group subcommand</code>) of a {@link CommandParent}.
 * <p>
 * Subcommands join the group by setting {@link Command#group()}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
@SuppressWarnings("unused")
public @interface CommandGroup {
    /**
     * Must be lowercase, and unique between the groups and direct subcommands of the parent
     *
     * @return The group name
     */
    String name();

    /**
     * @return The group description
     */
    String description();
}
