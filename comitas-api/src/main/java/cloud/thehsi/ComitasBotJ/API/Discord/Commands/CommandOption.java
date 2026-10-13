package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to declare command options (arguments), for the non-dynamic
 * method of adding commands, as a way to have clearly typed command arguments in a fast mannor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@SuppressWarnings("unused")
public @interface CommandOption {
    /**
     * Get the name of the option   1
     *
     * @return The name of the option
     */
    String name();

    /**
     * Get the descripion of the option
     *
     * @return The description of the option
     */
    String description();

    /**
     * Is this option required, options value will be null if not required and not present
     *
     * @return Is this option required
     */
    boolean required() default true;
}

