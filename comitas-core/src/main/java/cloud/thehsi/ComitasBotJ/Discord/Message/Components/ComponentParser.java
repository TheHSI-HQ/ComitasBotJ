package cloud.thehsi.ComitasBotJ.Discord.Message.Components;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Style;

import java.util.ArrayList;
import java.util.List;

public class ComponentParser {
    private ComponentParser() {
    }

    public static String parseComponent(Component component) {
        List<String> lines = new ArrayList<>();
        String currentLine = "";
        Style lastStyle = Style.RESET;
        String pendingAppend = "";

        Component _component = Component.empty().append(new Component(component));

        for (Component c : _component.children()) {
            int line_count = c.content().split("\n", -1).length;

            for (String line : c.content().split("\n", -1)) {
                line_count--;

                // Extremely complicated BLACK MAGIC, so this:
                // This is* a *test!!! becomes: This is *a* test!!!
                int start = 0;
                while (start < line.length() && Character.isWhitespace(line.charAt(start))) {
                    start++;
                }

                int end = line.length();
                while (end > start && Character.isWhitespace(line.charAt(end - 1))) {
                    end--;
                }

                StyleDifference styleDifference = new StyleDifference(lastStyle, c.style());

                // Close the previous component's formatting first.
                currentLine = closeStyle(currentLine, styleDifference);

                // Whitespace (trailing from the previous component, and this
                // component's own leading whitespace) sits entirely outside
                // formatting on both sides.
                currentLine += pendingAppend;
                currentLine += line.substring(0, start);

                // Open this component's formatting.
                currentLine = openStyle(currentLine, styleDifference);

                // Add this component's actual content.
                currentLine += line.substring(start, end);

                // Save trailing whitespace so it can be emitted outside formatting.
                pendingAppend = line.substring(end);

                if (line_count > 0) {
                    // Close this component's formatting before emitting trailing whitespace.
                    currentLine = closeStyle(
                            currentLine,
                            new StyleDifference(c.style(), Style.RESET)
                    );

                    currentLine += pendingAppend;
                    pendingAppend = "";

                    lines.add(currentLine);
                    currentLine = "";
                }
            }

            lastStyle = c.style();
        }

        // Close the final component's formatting.
        currentLine = closeStyle(
                currentLine,
                new StyleDifference(lastStyle, Style.RESET)
        );

        // Trailing whitespace must be outside the formatting.
        currentLine += pendingAppend;

        lines.add(currentLine);

        return String.join("\n", lines);
    }

    private static String setLineType(String line, boolean big, boolean small, boolean smaller, boolean sub, boolean quote, boolean bullet) {
        line = line.replaceFirst("(^#{1,3}|-#|-|>) ", "");
        if (big) return "# " + line;
        if (small) return "## " + line;
        if (smaller) return "### " + line;
        if (sub) return "-# " + line;
        if (quote) return "> " + line;
        if (bullet) return "- " + line;
        return line;
    }

    private static String closeStyle(String line, StyleDifference styleDifference) {
        if (styleDifference.spoiler.isFalse()) line += "||";
        if (styleDifference.code.isFalse()) line += "`";
        if (styleDifference.strikethrough.isFalse()) line += "~~";
        if (styleDifference.underline.isFalse()) line += "__";
        if (styleDifference.italic.isFalse()) line += "*";
        if (styleDifference.bold.isFalse()) line += "**";
        if (styleDifference.codeBlock.isFalse()) line += "\n```";

        return line;
    }

    private static String openStyle(String line, StyleDifference styleDifference) {
        if (styleDifference.bold.isTrue()) line += "**";
        if (styleDifference.italic.isTrue()) line += "*";
        if (styleDifference.underline.isTrue()) line += "__";
        if (styleDifference.strikethrough.isTrue()) line += "~~";
        if (styleDifference.code.isTrue()) line += "`";
        if (styleDifference.spoiler.isTrue()) line += "||";

        if (styleDifference.bigHeader.isTrue())
            line = setLineType(line, true, false, false, false, false, false);
        if (styleDifference.smallHeader.isTrue())
            line = setLineType(line, false, true, false, false, false, false);
        if (styleDifference.smallerHeader.isTrue())
            line = setLineType(line, false, false, true, false, false, false);
        if (styleDifference.subtext.isTrue())
            line = setLineType(line, false, false, false, true, false, false);
        if (styleDifference.quote.isTrue())
            line = setLineType(line, false, false, false, false, true, false);
        if (styleDifference.bulletPoints.isTrue())
            line = setLineType(line, false, false, false, false, false, true);
        if (styleDifference.codeBlock.isTrue()) line += "```";

        return line;
    }

    @SuppressWarnings("unused")
    private record BooleanDifference(Boolean value) {
        public static final BooleanDifference SAME = new BooleanDifference(null);
        public static final BooleanDifference TRUE = new BooleanDifference(true);
        public static final BooleanDifference FALSE = new BooleanDifference(false);

        public static BooleanDifference fromDiff(boolean old_value, boolean new_value) {
            if (old_value == new_value) return BooleanDifference.SAME;
            if (new_value) return BooleanDifference.TRUE;
            return BooleanDifference.FALSE;
        }

        public boolean isTrue() {
            return value == Boolean.TRUE;
        }

        public boolean isFalse() {
            return value == Boolean.FALSE;
        }
    }

    @SuppressWarnings("unused")
    private static class StyleDifference {
        final BooleanDifference bold;
        final BooleanDifference italic;
        final BooleanDifference underline;
        final BooleanDifference strikethrough;
        final BooleanDifference bigHeader;
        final BooleanDifference smallHeader;
        final BooleanDifference smallerHeader;
        final BooleanDifference subtext;
        final BooleanDifference quote;
        final BooleanDifference bulletPoints;
        final BooleanDifference code;
        final BooleanDifference codeBlock;
        final BooleanDifference spoiler;

        public StyleDifference(Style old_style, Style new_style) {
            bold = BooleanDifference.fromDiff(old_style.isBold(), new_style.isBold());
            italic = BooleanDifference.fromDiff(old_style.isItalic(), new_style.isItalic());
            underline = BooleanDifference.fromDiff(old_style.isUnderline(), new_style.isUnderline());
            strikethrough = BooleanDifference.fromDiff(old_style.isStrikethrough(), new_style.isStrikethrough());
            bigHeader = BooleanDifference.fromDiff(old_style.isBigHeader(), new_style.isBigHeader());
            smallHeader = BooleanDifference.fromDiff(old_style.isSmallHeader(), new_style.isSmallHeader());
            smallerHeader = BooleanDifference.fromDiff(old_style.isSmallerHeader(), new_style.isSmallerHeader());
            subtext = BooleanDifference.fromDiff(old_style.isSubtext(), new_style.isSubtext());
            quote = BooleanDifference.fromDiff(old_style.isQuote(), new_style.isQuote());
            bulletPoints = BooleanDifference.fromDiff(old_style.isBulletPoints(), new_style.isBulletPoints());
            code = BooleanDifference.fromDiff(old_style.isCode(), new_style.isCode());
            codeBlock = BooleanDifference.fromDiff(old_style.isCodeBlock(), new_style.isCodeBlock());
            spoiler = BooleanDifference.fromDiff(old_style.isSpoiler(), new_style.isSpoiler());
        }
    }
}