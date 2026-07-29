package cloud.thehsi.ComitasBotJ.Discord.Commands;

public record CommandArgument<T>(CommandArgumentType<T> type, String name, String description, boolean required) {
}
