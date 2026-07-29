package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@SuppressWarnings("unused")
public @interface Command {
    String name();
    String description();
}

