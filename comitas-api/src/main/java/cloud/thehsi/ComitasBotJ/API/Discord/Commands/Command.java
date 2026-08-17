package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@SuppressWarnings("unused")
public @interface Command {
    String name();
    String description();
    boolean nsfw() default false;
    CommandType[] commandType() default { CommandType.GUILD_INSTALL };
    CommandContextType[] commandContextType() default { CommandContextType.GUILD };
}

