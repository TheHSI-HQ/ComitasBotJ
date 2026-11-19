package cloud.thehsi.ComitasBotJ.API.Utility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A parser for universal commands and arguments
 */
public class CommandSyntaxParser {
    /**
     * Represents a command that has been parsed
     *
     * @param command Only command name, without prefix or arguments
     * @param args    Only the command arguments, without the command nor its prefix
     */
    public record ParsedCommand(String command, String[] args) {}

    /**
     * @param rawCommand The raw unparsed possibly command straight from discord
     * @param prefix     The prefix to lookout for
     * @return           The parsed command, if found, otherwise null
     */
    @Nullable
    public static ParsedCommand parse(@NotNull String rawCommand, @NotNull String prefix) {
        if (!rawCommand.startsWith(prefix))
            return null;

        List<String> args = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        StringBuilder commandName = new StringBuilder();
        boolean inArg = false;

        boolean escaped = false;
        boolean quoted = false;

        for (int i = 0; i < rawCommand.length(); i++) {
            char c = rawCommand.charAt(i);

            if (i < prefix.length())
                continue;

            if (!inArg) {
                if (Character.isWhitespace(c))
                    inArg = true;
                else
                    commandName.append(c);
                continue;
            }

            if (!quoted && !escaped && Character.isWhitespace(c)) {
                args.add(buffer.toString());
                buffer = new StringBuilder();
                continue;
            }

            if (!escaped && c == '"') {
                quoted = !quoted;
                continue;
            }
            if (escaped) {
                escaped = false;
                if (List.of('"', '\\', ' ').contains(c))
                    buffer.append(c);
                else
                    throw new RuntimeException("Invalid escape sequence");
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }

            buffer.append(c);
        }

        args.add(buffer.toString());
        return new ParsedCommand(commandName.toString(), args.toArray(new String[0]));
    }
}
