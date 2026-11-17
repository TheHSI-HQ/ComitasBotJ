package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container for repeated {@link CommandParent} annotations, there's no need to use this directly
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SuppressWarnings("unused")
public @interface CommandParents {
    /**
     * @return The declared parents
     */
    CommandParent[] value();
}
