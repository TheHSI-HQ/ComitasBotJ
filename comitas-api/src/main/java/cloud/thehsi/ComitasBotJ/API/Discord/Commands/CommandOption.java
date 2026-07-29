package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@SuppressWarnings("unused")
public @interface CommandOption {
    String name();
    String description();
    boolean required() default true;
}

