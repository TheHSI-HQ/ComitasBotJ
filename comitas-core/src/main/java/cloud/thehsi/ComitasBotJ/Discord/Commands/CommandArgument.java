package cloud.thehsi.ComitasBotJ.Discord.Commands;

import org.jetbrains.annotations.Nullable;

public record CommandArgument<T>(CommandArgumentType<T> type, @Nullable String name, @Nullable String description,
                                 boolean required) {
}
