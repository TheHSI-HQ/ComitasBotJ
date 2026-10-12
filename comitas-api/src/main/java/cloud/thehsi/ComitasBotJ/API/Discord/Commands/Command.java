package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describe a command, with a name and a description,
 * as well as an optional nsfw, commandType and commandContextType
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@SuppressWarnings("unused")
public @interface Command {
    /**
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
}

