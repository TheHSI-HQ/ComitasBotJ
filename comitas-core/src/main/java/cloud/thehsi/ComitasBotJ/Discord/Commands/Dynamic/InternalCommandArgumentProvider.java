package cloud.thehsi.ComitasBotJ.Discord.Commands.Dynamic;

import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgument;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentProvider;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class InternalCommandArgumentProvider implements CommandArgumentProvider {
    @NotNull Map<String, CommandArgument<?>> argumentMap = new HashMap<>();

    public InternalCommandArgumentProvider() {
    }

    public void addArgument(@NotNull String identifier, @NotNull CommandArgument<?> argument) {
        argumentMap.put(identifier, argument);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T> CommandArgument<T> get(
            @NotNull String identifier,
            @NotNull CommandArgumentType<T> type
    ) {
        CommandArgument<?> argument = argumentMap.get(identifier);
        if (argument == null)
            return null;
        if (argument.getType().type() != type.type())
            return null;
        return (CommandArgument<T>) argument;
    }

    @Override
    public <T> boolean has(@NotNull String identifier) {
        return argumentMap.containsKey(identifier);
    }

    @Override
    public <T> boolean has(@NotNull String identifier, @NotNull CommandArgumentType<T> type) {
        CommandArgument<?> argument = argumentMap.get(identifier);
        if (argument == null)
            return false;
        return argument.getType().type() == type.type();
    }
}
