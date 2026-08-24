package cloud.thehsi.ComitasBotJ.Discord.Commands.Dynamic;

import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgument;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InternalCommandArgument<T> implements CommandArgument<T> {
    @Nullable Object value;
    boolean optional;
    @NotNull CommandArgumentType<T> type;

    public InternalCommandArgument(@Nullable Object value, boolean optional, @NotNull CommandArgumentType<T> type) {
        this.value = value;
        this.optional = optional;
        this.type = type;
    }

    @Override
    public @Nullable T getValue() {
        return type.cast(value);
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public @NotNull CommandArgumentType<T> getType() {
        return type;
    }
}
