package cloud.thehsi.ComitasBotJ.Discord.Message.Components;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Style;

import java.util.ArrayList;
import java.util.List;

public class ComponentParser {
    private ComponentParser() {}

    private record BooleanDifference(Boolean value) {
        public static final BooleanDifference SAME = new BooleanDifference(null);
        public static final BooleanDifference TRUE = new BooleanDifference(true);
        public static final BooleanDifference FALSE = new BooleanDifference(false);

        public boolean isDifferent() {
            return value != null;
        }

        public boolean isTrue() {
            return value == Boolean.TRUE;
        }

        public boolean isFalse() {
            return value == Boolean.FALSE;
        }

        public static BooleanDifference fromDiff(boolean old_value, boolean new_value) {
            if (old_value == new_value) return BooleanDifference.SAME;
            if (new_value) return BooleanDifference.TRUE;
            return BooleanDifference.FALSE;
        }
    }

    private static class StyleDifference {
        BooleanDifference bold;
        BooleanDifference italic;
        BooleanDifference underline;
        BooleanDifference strikethrough;
        BooleanDifference bigHeader;
        BooleanDifference smallHeader;
        BooleanDifference smallerHeader;
        BooleanDifference subtext;
        BooleanDifference quote;
        BooleanDifference bulletPoints;
        BooleanDifference code;
        BooleanDifference codeBlock;
        BooleanDifference spoiler;

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

    public static String parseComponent(Component component) {
        List<String> lines = new ArrayList<>();
        String currentLine = "";
        Style lastStyle = Style.RESET;

        Component _component = Component.empty().append(component);

        for (Component c : _component.children()) {
            int line_count = c.content().split("\n", -1).length; // -1 is required so "\n" -!> [] but ["", ""]
            for (String line : c.content().split("\n", -1)) {
                line_count--;
                currentLine = applyStyle(currentLine, new StyleDifference(lastStyle, c.style()));
                currentLine += line;

                if (line_count > 0) {
                    lines.add(applyStyle(currentLine, new StyleDifference(lastStyle, Style.RESET)));
                    currentLine = "";
                }
            }
            lastStyle = c.style();
        }

        lines.add(applyStyle(currentLine, new StyleDifference(lastStyle, Style.RESET)));

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

    private static String applyStyle(String line, StyleDifference styleDifference) {
        if (styleDifference.bold.isDifferent()) line += "**";
        if (styleDifference.italic.isDifferent()) line += "*";
        if (styleDifference.underline.isDifferent()) line += "__";
        if (styleDifference.strikethrough.isDifferent()) line += "~~";
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
        if (styleDifference.code.isDifferent()) line += "`";
        if (styleDifference.codeBlock.isTrue()) line += "```";
        if (styleDifference.codeBlock.isFalse()) line += "\n```";
        if (styleDifference.spoiler.isDifferent()) line += "||";

        return line;
    }
}