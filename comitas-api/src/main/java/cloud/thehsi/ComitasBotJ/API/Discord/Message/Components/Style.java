package cloud.thehsi.ComitasBotJ.API.Discord.Message.Components;

@SuppressWarnings("unused")
public class Style {
    public static final Style RESET = Style.fromMask(0);
    public static final Style BOLD = Style.fromMask(1);
    public static final Style ITALIC = Style.fromMask(2 << 1);
    public static final Style UNDERLINE = Style.fromMask(2 << 2);
    public static final Style STRIKETHROUGH = Style.fromMask(2 << 3);

    public static final Style BIG_HEADER = Style.fromMask(2 << 4);
    public static final Style SMALL_HEADER = Style.fromMask(2 << 5);
    public static final Style SMALLER_HEADER = Style.fromMask(2 << 6);
    public static final Style SUBTEXT = Style.fromMask(2 << 7);

    public static final Style QUOTE = Style.fromMask(2 << 8);

    public static final Style BULLET_POINTS = Style.fromMask(2 << 9);

    public static final Style CODE = Style.fromMask(2 << 10);
    public static final Style CODE_BLOCK = Style.fromMask(2 << 11);

    public static final Style SPOILER = Style.fromMask(2 << 12);

    private boolean bold;
    private boolean italic;
    private boolean underline;
    private boolean strikethrough;
    private boolean bigHeader;
    private boolean smallHeader;
    private boolean smallerHeader;
    private boolean subtext;
    private boolean quote;
    private boolean bulletPoints;
    private boolean code;
    private boolean codeBlock;
    private boolean spoiler;

    public boolean isBold() {
        return bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public boolean isUnderline() {
        return underline;
    }

    public boolean isStrikethrough() {
        return strikethrough;
    }

    public boolean isBigHeader() {
        return bigHeader;
    }

    public boolean isSmallHeader() {
        return smallHeader;
    }

    public boolean isSmallerHeader() {
        return smallerHeader;
    }

    public boolean isSubtext() {
        return subtext;
    }

    public boolean isQuote() {
        return quote;
    }

    public boolean isBulletPoints() {
        return bulletPoints;
    }

    public boolean isCode() {
        return code;
    }

    public boolean isCodeBlock() {
        return codeBlock;
    }

    public boolean isSpoiler() {
        return spoiler;
    }

    private Style(boolean bold, boolean italic, boolean underline, boolean strikethrough, boolean bigHeader, boolean smallHeader, boolean smallerHeader, boolean subtext, boolean quote, boolean bulletPoints, boolean code, boolean codeBlock, boolean spoiler) {
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.strikethrough = strikethrough;
        this.bigHeader = bigHeader;
        this.smallHeader = smallHeader;
        this.smallerHeader = smallerHeader;
        this.subtext = subtext;
        this.quote = quote;
        this.bulletPoints = bulletPoints;
        this.code = code;
        this.codeBlock = codeBlock;
        this.spoiler = spoiler;
    }

    public static Style fromMask(int mask) {
        return new Style(
                (mask & 1) != 0,
                (mask & 2 << 1) != 0,
                (mask & 2 << 2) != 0,
                (mask & 2 << 3) != 0,
                (mask & 2 << 4) != 0,
                (mask & 2 << 5) != 0,
                (mask & 2 << 6) != 0,
                (mask & 2 << 7) != 0,
                (mask & 2 << 8) != 0,
                (mask & 2 << 9) != 0,
                (mask & 2 << 10) != 0,
                (mask & 2 << 11) != 0,
                (mask & 2 << 12) != 0
        );
    }

    public Style add(Style style) {
        this.bold |= style.bold;
        this.italic |= style.italic;
        this.underline |= style.underline;
        this.strikethrough |= style.strikethrough;
        this.bigHeader |= style.bigHeader;
        this.smallHeader |= style.smallHeader;
        this.smallerHeader |= style.smallerHeader;
        this.subtext |= style.subtext;
        this.quote |= style.quote;
        this.bulletPoints |= style.bulletPoints;
        this.code |= style.code;
        this.codeBlock |= style.codeBlock;
        this.spoiler |= style.spoiler;

        return this;
    }

    public Style remove(Style style) {
        this.bold = this.bold & !style.bold;
        this.italic = this.italic & !style.italic;
        this.underline = this.underline & !style.underline;
        this.strikethrough = this.strikethrough & !style.strikethrough;
        this.bigHeader = this.bigHeader & !style.bigHeader;
        this.smallHeader = this.smallHeader & !style.smallHeader;
        this.smallerHeader = this.smallerHeader & !style.smallerHeader;
        this.subtext = this.subtext & !style.subtext;
        this.quote = this.quote & !style.quote;
        this.bulletPoints = this.bulletPoints & !style.bulletPoints;
        this.code = this.code & !style.code;
        this.codeBlock = this.codeBlock & !style.codeBlock;
        this.spoiler = this.spoiler & !style.spoiler;

        return this;
    }
}