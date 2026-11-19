package cloud.thehsi.ComitasBotJ.API.Utility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A parser for universal commands and arguments
 */
public class CommandSyntaxParser {
    /**
     * Converts each argument of a command to its corresponding type
     *
     * @param command The parsed command whose arguments to convert
     * @param types   The expected type of each argument, in order
     * @return The converted arguments
     * @throws IllegalArgumentException If {@link ArgumentType#GREEDY_STRING} is used anywhere but as the last type
     * @throws CommandArgumentException If the amount of arguments does not match the amount of types or an argument could not be converted to its type
     */
    @NotNull
    public static ParsedArguments parseArguments(@NotNull ParsedCommand command, @NotNull ArgumentType... types) {
        for (int i = 0; i < types.length - 1; i++)
            if (types[i] == ArgumentType.GREEDY_STRING)
                throw new IllegalArgumentException("GREEDY_STRING must be the last argument type");

        List<String> args = command.args();
        boolean greedy = types.length > 0 && types[types.length - 1] == ArgumentType.GREEDY_STRING;
        if (args.size() < types.length || (!greedy && args.size() > types.length))
            throw new CommandArgumentException("Expected " + (greedy ? "at least " : "") + types.length + " argument(s) but got " + args.size(), Math.min(args.size(), types.length));

        List<Object> values = new ArrayList<>(types.length);
        for (int i = 0; i < types.length; i++) {
            if (types[i] == ArgumentType.GREEDY_STRING)
                values.add(command.rawTails().get(i));
            else
                values.add(parseArgument(args.get(i), types[i], i));
        }
        return new ParsedArguments(values, List.of(types));
    }

    @NotNull
    private static Object parseArgument(@NotNull String arg, @NotNull ArgumentType type, int index) {
        try {
            return switch (type) {
                case STRING, GREEDY_STRING -> arg;
                case INTEGER -> Integer.parseInt(arg);
                case LONG -> Long.parseLong(arg);
                case DOUBLE -> {
                    double value = Double.parseDouble(arg);
                    if (!Double.isFinite(value))
                        throw new NumberFormatException();
                    yield value;
                }
                case BOOLEAN -> switch (arg.toLowerCase(Locale.ROOT)) {
                    case "true", "yes" -> true;
                    case "false", "no" -> false;
                    default -> throw new CommandArgumentException("\"" + arg + "\" is not a valid boolean", index);
                };
                case USER -> parseMention(arg, index, "<@!", "<@");
                case CHANNEL -> parseMention(arg, index, "<#");
                case ROLE -> parseMention(arg, index, "<@&");
            };
        } catch (NumberFormatException e) {
            throw new CommandArgumentException("\"" + arg + "\" is not a valid " + type.name().toLowerCase(Locale.ROOT), index);
        }
    }

    private static long parseMention(@NotNull String arg, int index, @NotNull String... openings) {
        String id = arg;
        for (String opening : openings) {
            if (arg.startsWith(opening) && arg.endsWith(">")) {
                id = arg.substring(opening.length(), arg.length() - 1);
                break;
            }
        }

        if (id.isEmpty() || !id.chars().allMatch(c -> c >= '0' && c <= '9'))
            throw new NumberFormatException();
        return Long.parseLong(id);
    }

    /**
     * @param rawCommand The raw unparsed possibly command straight from discord
     * @param prefix     The prefix to lookout for
     * @return           The parsed command, if found, otherwise null
     * @throws CommandSyntaxException If the arguments contain an invalid escape sequence, an unclosed quote or a trailing backslash
     */
    @Nullable
    public static ParsedCommand parse(@NotNull String rawCommand, @NotNull String prefix) {
        if (!rawCommand.startsWith(prefix))
            return null;

        int i = prefix.length();
        StringBuilder commandName = new StringBuilder();
        for (; i < rawCommand.length(); i++) {
            char c = rawCommand.charAt(i);
            if (Character.isWhitespace(c))
                break;
            commandName.append(c);
        }

        if (commandName.isEmpty())
            return null;

        List<String> args = new ArrayList<>();
        List<String> rawTails = new ArrayList<>();
        String rawTrimmed = rawCommand.stripTrailing();
        StringBuilder buffer = new StringBuilder();
        boolean argStarted = false;

        boolean escaped = false;
        boolean quoted = false;
        int quoteStart = -1;

        for (; i < rawCommand.length(); i++) {
            char c = rawCommand.charAt(i);

            if (escaped) {
                escaped = false;
                if (c == '"' || c == '\\' || Character.isWhitespace(c))
                    buffer.append(c);
                else
                    throw new CommandSyntaxException("Invalid escape sequence \\" + c, i - 1);
                continue;
            }

            if (!quoted && Character.isWhitespace(c)) {
                if (argStarted) {
                    args.add(buffer.toString());
                    buffer = new StringBuilder();
                    argStarted = false;
                }
                continue;
            }

            if (!argStarted) {
                argStarted = true;
                rawTails.add(rawTrimmed.substring(i));
            }

            if (c == '"') {
                quoted = !quoted;
                quoteStart = i;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }

            buffer.append(c);
        }

        if (escaped)
            throw new CommandSyntaxException("Trailing backslash", rawCommand.length() - 1);
        if (quoted)
            throw new CommandSyntaxException("Unclosed quote", quoteStart);

        if (argStarted)
            args.add(buffer.toString());

        return new ParsedCommand(commandName.toString(), args, rawTails);
    }

    /**
     * The types a command argument can be converted to using {@link #parseArguments(ParsedCommand, ArgumentType...)}
     */
    public enum ArgumentType {
        /**
         * Any text, returned as {@link String}
         */
        STRING(String.class),
        /**
         * The rest of the message starting at this argument, returned as {@link String}.
         * It is taken verbatim, so quotes and escapes are kept as typed and whitespace between words is preserved.
         * Must be the last type
         */
        GREEDY_STRING(String.class),
        /**
         * A whole number within the int range, returned as {@link Integer}
         */
        INTEGER(Integer.class),
        /**
         * A whole number within the long range, returned as {@link Long}
         */
        LONG(Long.class),
        /**
         * A finite decimal number, returned as {@link Double}
         */
        DOUBLE(Double.class),
        /**
         * {@code true}/{@code yes} or {@code false}/{@code no}, case-insensitive, returned as {@link Boolean}
         */
        BOOLEAN(Boolean.class),
        /**
         * A user mention ({@code <@id>} or {@code <@!id>}) or raw user id, returned as the id as {@link Long}
         */
        USER(Long.class),
        /**
         * A channel mention ({@code <#id>}) or raw channel id, returned as the id as {@link Long}
         */
        CHANNEL(Long.class),
        /**
         * A role mention ({@code <@&id>}) or raw role id, returned as the id as {@link Long}
         */
        ROLE(Long.class);

        private final @NotNull Class<?> type;

        ArgumentType(@NotNull Class<?> type) {
            this.type = type;
        }

        /**
         * @return The class of the values this argument type is converted to
         */
        @NotNull
        public Class<?> getType() {
            return type;
        }
    }

    /**
     * Represents a command that has been parsed
     *
     * @param command  Only command name, without prefix or arguments
     * @param args     Only the command arguments, without the command nor its prefix
     * @param rawTails The unprocessed message text from the start of each argument to the end of the message, with trailing whitespace removed
     */
    public record ParsedCommand(String command, List<String> args, List<String> rawTails) {
        /**
         * @param command  Only command name, without prefix or arguments
         * @param args     Only the command arguments, without the command nor its prefix, copied into an unmodifiable list
         * @param rawTails The unprocessed message text from the start of each argument to the end of the message, with trailing whitespace removed, copied into an unmodifiable list
         * @throws IllegalArgumentException If {@code args} and {@code rawTails} differ in size
         */
        public ParsedCommand {
            if (args.size() != rawTails.size())
                throw new IllegalArgumentException("args and rawTails must be the same size");
            args = List.copyOf(args);
            rawTails = List.copyOf(rawTails);
        }
    }

    /**
     * Command arguments that have been converted to their types using {@link #parseArguments(ParsedCommand, ArgumentType...)}
     */
    public static final class ParsedArguments {
        private final @NotNull List<Object> values;
        private final @NotNull List<ArgumentType> types;

        private ParsedArguments(@NotNull List<Object> values, @NotNull List<ArgumentType> types) {
            this.values = List.copyOf(values);
            this.types = List.copyOf(types);
        }

        /**
         * @return The amount of arguments
         */
        public int size() {
            return values.size();
        }

        /**
         * @param index The index of the argument
         * @return The type the argument at the given index was parsed as
         * @throws IndexOutOfBoundsException If there is no argument at the given index
         */
        @NotNull
        public ArgumentType getType(int index) {
            return types.get(index);
        }

        /**
         * @param index The index of the argument
         * @return The argument at the given index
         * @throws IndexOutOfBoundsException If there is no argument at the given index
         * @throws IllegalStateException     If the argument is not of type {@link ArgumentType#STRING} or {@link ArgumentType#GREEDY_STRING}
         */
        @NotNull
        public String getString(int index) {
            return get(index, String.class, ArgumentType.STRING, ArgumentType.GREEDY_STRING);
        }

        /**
         * @param index The index of the argument
         * @return The argument at the given index
         * @throws IndexOutOfBoundsException If there is no argument at the given index
         * @throws IllegalStateException     If the argument is not of type {@link ArgumentType#INTEGER}
         */
        public int getInteger(int index) {
            return get(index, Integer.class, ArgumentType.INTEGER);
        }

        /**
         * @param index The index of the argument
         * @return The argument at the given index
         * @throws IndexOutOfBoundsException If there is no argument at the given index
         * @throws IllegalStateException     If the argument is not of type {@link ArgumentType#LONG}
         */
        public long getLong(int index) {
            return get(index, Long.class, ArgumentType.LONG);
        }

        /**
         * @param index The index of the argument
         * @return The argument at the given index
         * @throws IndexOutOfBoundsException If there is no argument at the given index
         * @throws IllegalStateException     If the argument is not of type {@link ArgumentType#DOUBLE}
         */
        public double getDouble(int index) {
            return get(index, Double.class, ArgumentType.DOUBLE);
        }

        /**
         * @param index The index of the argument
         * @return The argument at the given index
         * @throws IndexOutOfBoundsException If there is no argument at the given index
         * @throws IllegalStateException     If the argument is not of type {@link ArgumentType#BOOLEAN}
         */
        public boolean getBoolean(int index) {
            return get(index, Boolean.class, ArgumentType.BOOLEAN);
        }

        /**
         * @param index The index of the argument
         * @return The user id of the argument at the given index
         * @throws IndexOutOfBoundsException If there is no argument at the given index
         * @throws IllegalStateException     If the argument is not of type {@link ArgumentType#USER}
         */
        public long getUserId(int index) {
            return get(index, Long.class, ArgumentType.USER);
        }

        /**
         * @param index The index of the argument
         * @return The channel id of the argument at the given index
         * @throws IndexOutOfBoundsException If there is no argument at the given index
         * @throws IllegalStateException     If the argument is not of type {@link ArgumentType#CHANNEL}
         */
        public long getChannelId(int index) {
            return get(index, Long.class, ArgumentType.CHANNEL);
        }

        /**
         * @param index The index of the argument
         * @return The role id of the argument at the given index
         * @throws IndexOutOfBoundsException If there is no argument at the given index
         * @throws IllegalStateException     If the argument is not of type {@link ArgumentType#ROLE}
         */
        public long getRoleId(int index) {
            return get(index, Long.class, ArgumentType.ROLE);
        }

        @NotNull
        private <T> T get(int index, @NotNull Class<T> clazz, @NotNull ArgumentType... allowed) {
            ArgumentType type = types.get(index);
            if (!List.of(allowed).contains(type))
                throw new IllegalStateException("Argument " + index + " is of type " + type + ", not " + List.of(allowed));
            return clazz.cast(values.get(index));
        }

        @Override
        public @NotNull String toString() {
            return "ParsedArguments" + values;
        }
    }
}
