package cloud.thehsi.ComitasBotJ.Discord.Message.Components;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentUnparser {
    private ComponentUnparser() {
    }

    private @NotNull
    static final Pattern BLOCK_PREFIX = Pattern.compile("^(#{1,3}|-#|>|-) (.*)$");
    private @NotNull
    static final Pattern FENCE = Pattern.compile("^```(\\S*)\\s*$");

    @NotNull
    public static Component unparseComponent(@NotNull String message) {
        Component root = Component.empty();
        String[] rawLines = message.split("\n", -1);

        boolean inCodeBlock = false;
        List<String> codeBlockLines = new ArrayList<>();

        for (int i = 0; i < rawLines.length; i++) {
            String rawLine = rawLines[i];

            if (!inCodeBlock && FENCE.matcher(rawLine).matches()) {
                inCodeBlock = true;
                codeBlockLines.clear();
                continue;
            }

            if (inCodeBlock) {
                if (FENCE.matcher(rawLine).matches()) {
                    inCodeBlock = false;
                    root = root.append(Component.text(String.join("\n", codeBlockLines), Style.CODE_BLOCK));
                    if (i < rawLines.length - 1) root = root.append("\n");
                    continue;
                }
                codeBlockLines.add(rawLine);
                continue;
            }

            String content = rawLine;
            Style lineStyle = null;

            Matcher blockMatcher = BLOCK_PREFIX.matcher(rawLine);
            if (blockMatcher.matches()) {
                content = blockMatcher.group(2);
                lineStyle = switch (blockMatcher.group(1)) {
                    case "#" -> Style.BIG_HEADER;
                    case "##" -> Style.SMALL_HEADER;
                    case "###" -> Style.SMALLER_HEADER;
                    case "-#" -> Style.SUBTEXT;
                    case ">" -> Style.QUOTE;
                    case "-" -> Style.BULLET_POINTS;
                    default -> null;
                };
            }

            root = root.append(parseInline(content, lineStyle));

            if (i < rawLines.length - 1) {
                root = root.append("\n");
            }
        }

        // Unterminated code fence in the input - flush whatever was collected.
        if (inCodeBlock) {
            root = root.append(Component.text(String.join("\n", codeBlockLines), Style.CODE_BLOCK));
        }

        return root;
    }

    @NotNull
    private static Component parseInline(@NotNull String line, @Nullable Style lineStyle) {
        Component result = Component.empty();

        boolean bold = false, italic = false, underline = false,
                strikethrough = false, code = false, spoiler = false;

        StringBuilder buffer = new StringBuilder();
        int i = 0;

        while (i < line.length()) {
            char ch = line.charAt(i);

            // Basic escaping: "\*" -> literal "*", etc.
            if (ch == '\\' && i + 1 < line.length() && isMarkerChar(line.charAt(i + 1))) {
                buffer.append(line.charAt(i + 1));
                i += 2;
                continue;
            }

            String rest = line.substring(i);
            String matchedMarker = null;

            if (rest.startsWith("**")) matchedMarker = "**";
            else if (rest.startsWith("__")) matchedMarker = "__";
            else if (rest.startsWith("~~")) matchedMarker = "~~";
            else if (rest.startsWith("||")) matchedMarker = "||";
            else if (rest.startsWith("`")) matchedMarker = "`";
            else if (rest.startsWith("*")) matchedMarker = "*";

            if (matchedMarker != null) {
                result = appendRun(result, buffer, lineStyle, bold, italic, underline, strikethrough, code, spoiler);

                switch (matchedMarker) {
                    case "**" -> bold = !bold;
                    case "__" -> underline = !underline;
                    case "~~" -> strikethrough = !strikethrough;
                    case "||" -> spoiler = !spoiler;
                    case "`" -> code = !code;
                    case "*" -> italic = !italic;
                }

                i += matchedMarker.length();
                continue;
            }

            buffer.append(ch);
            i++;
        }

        result = appendRun(result, buffer, lineStyle, bold, italic, underline, strikethrough, code, spoiler);

        return result;
    }

    private static boolean isMarkerChar(char c) {
        return c == '*' || c == '_' || c == '~' || c == '|' || c == '`' || c == '\\';
    }

    @NotNull
    private static Component appendRun(@NotNull Component target, @NotNull StringBuilder buffer, @Nullable Style lineStyle,
                                       boolean bold, boolean italic, boolean underline,
                                       boolean strikethrough, boolean code, boolean spoiler) {
        if (buffer.isEmpty()) return target;

        List<Style> styles = new ArrayList<>();
        if (bold) styles.add(Style.BOLD);
        if (italic) styles.add(Style.ITALIC);
        if (underline) styles.add(Style.UNDERLINE);
        if (strikethrough) styles.add(Style.STRIKETHROUGH);
        if (code) styles.add(Style.CODE);
        if (spoiler) styles.add(Style.SPOILER);
        if (lineStyle != null) styles.add(lineStyle);

        Component run = styles.isEmpty()
                ? Component.text(buffer.toString())
                : Component.text(buffer.toString(), styles.toArray(new Style[0]));

        buffer.setLength(0);

        return target.append(run);
    }
}