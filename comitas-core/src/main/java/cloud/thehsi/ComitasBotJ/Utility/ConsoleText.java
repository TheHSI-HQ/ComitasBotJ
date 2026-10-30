package cloud.thehsi.ComitasBotJ.Utility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Makes untrusted text (plugin metadata, plugin log output, Discord content) safe to print to the terminal.
 */
public final class ConsoleText {
    /**
     * SGR (color / style) sequences, the only escape sequences kept in log output.
     */
    private static final Pattern SGR = Pattern.compile("\u001B\\[[0-9;]{0,32}m");

    private ConsoleText() {
    }

    /**
     * Removes every control and format character, including line breaks and escape sequences.
     * Used for single line values like plugin names or permission reasons.
     */
    @NotNull
    public static String plain(@Nullable String text) {
        if (text == null) return "";
        StringBuilder result = new StringBuilder(text.length());
        String withoutSgr = SGR.matcher(text).replaceAll("");
        for (int i = 0; i < withoutSgr.length(); i++) {
            char c = withoutSgr.charAt(i);
            if (isUnsafe(c)) continue;
            result.append(c);
        }
        return result.toString();
    }

    /**
     * Keeps colors, line breaks and tabs, but removes every other control character and escape sequence
     * (cursor movement, screen clearing, OSC sequences like clipboard writes, carriage returns, ...).
     */
    @NotNull
    public static String forTerminal(@Nullable String text) {
        if (text == null) return "";
        StringBuilder result = new StringBuilder(text.length());
        var matcher = SGR.matcher(text);
        int last = 0;
        while (matcher.find()) {
            appendSafe(result, text, last, matcher.start());
            result.append(matcher.group());
            last = matcher.end();
        }
        appendSafe(result, text, last, text.length());
        return result.toString();
    }

    private static void appendSafe(@NotNull StringBuilder result, @NotNull String text, int from, int to) {
        for (int i = from; i < to; i++) {
            char c = text.charAt(i);
            if ((c == '\n' || c == '\t') || !isUnsafe(c)) result.append(c);
        }
    }

    private static boolean isUnsafe(char c) {
        return Character.isISOControl(c) || Character.getType(c) == Character.FORMAT
                || c == '\u2028' || c == '\u2029';
    }
}
