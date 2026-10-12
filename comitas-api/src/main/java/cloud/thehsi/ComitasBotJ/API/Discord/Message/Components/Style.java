package cloud.thehsi.ComitasBotJ.API.Discord.Message.Components;

import org.jetbrains.annotations.NotNull;

/**
 * Used for styling in components
 */
@SuppressWarnings("unused")
public class Style {
    /**
     * Resets all styles
     */
    public @NotNull
    static final Style RESET = Style.fromMask(0);
    /**
     * Makes the text <b>bold</b>
     */
    public @NotNull
    static final Style BOLD = Style.fromMask(1);
    /**
     * Makes the text <i>italic</i>
     */
    public @NotNull
    static final Style ITALIC = Style.fromMask(2 << 1);
    /**
     * <u>Underlines</u> the text
     */
    public @NotNull
    static final Style UNDERLINE = Style.fromMask(2 << 2);
    /**
     * <s>Strikes through</s> the text
     */
    public @NotNull
    static final Style STRIKETHROUGH = Style.fromMask(2 << 3);

    /**
     * Marks this line as a <h1>big header</h1>
     */
    public @NotNull
    static final Style BIG_HEADER = Style.fromMask(2 << 4);
    /**
     * Marks this line as a <h1>big header</h1>
     */
    public @NotNull
    static final Style SMALL_HEADER = Style.fromMask(2 << 5);
    /**
     * Marks this line as a <h3>smaller header</h3>
     */
    public @NotNull
    static final Style SMALLER_HEADER = Style.fromMask(2 << 6);

    /**
     * Marks this line as a subtext
     */
    public @NotNull
    static final Style SUBTEXT = Style.fromMask(2 << 7);

    /**
     * Marks this line as a quote
     */
    public @NotNull
    static final Style QUOTE = Style.fromMask(2 << 8);

    /**
     * Marks this line as a <li>bullet point</li>
     */
    public @NotNull
    static final Style BULLET_POINTS = Style.fromMask(2 << 9);

    /**
     * Makes this text <code>code</code>
     */
    public @NotNull
    static final Style CODE = Style.fromMask(2 << 10);
    /**
     * Marks this line as a code block
     */
    public @NotNull
    static final Style CODE_BLOCK = Style.fromMask(2 << 11);

    /**
     * Makes this text a spoiler
     */
    public @NotNull
    static final Style SPOILER = Style.fromMask(2 << 12);

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

    /**
     * Copies a style
     *
     * @param style The style to copy
     */
    public Style(@NotNull Style style) {
        this.bold = style.bold;
        this.italic = style.italic;
        this.underline = style.underline;
        this.strikethrough = style.strikethrough;
        this.bigHeader = style.bigHeader;
        this.smallHeader = style.smallHeader;
        this.smallerHeader = style.smallerHeader;
        this.subtext = style.subtext;
        this.quote = style.quote;
        this.bulletPoints = style.bulletPoints;
        this.code = style.code;
        this.codeBlock = style.codeBlock;
        this.spoiler = style.spoiler;
    }

    /**
     * Converts a mask to a style
     *
     * @param mask The mask to convert to a style
     * @return The converted style
     */
    @NotNull
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

    /**
     * @return Is this style bold
     */
    public boolean isBold() {
        return bold;
    }

    /**
     * @return Is this style italic
     */
    public boolean isItalic() {
        return italic;
    }

    /**
     * @return Is this style underline
     */
    public boolean isUnderline() {
        return underline;
    }

    /**
     * @return Is this style strikethrough
     */
    public boolean isStrikethrough() {
        return strikethrough;
    }

    /**
     * @return Is this style a big header
     */
    public boolean isBigHeader() {
        return bigHeader;
    }

    /**
     * @return Is this style a small header
     */
    public boolean isSmallHeader() {
        return smallHeader;
    }

    /**
     * @return Is this style a smaller header
     */
    public boolean isSmallerHeader() {
        return smallerHeader;
    }

    /**
     * @return Is this style subtext
     */
    public boolean isSubtext() {
        return subtext;
    }

    /**
     * @return Is this style a quote
     */
    public boolean isQuote() {
        return quote;
    }

    /**
     * @return Is this style a bulletpoint
     */
    public boolean isBulletPoints() {
        return bulletPoints;
    }

    /**
     * @return Is this style code
     */
    public boolean isCode() {
        return code;
    }

    /**
     * @return Is this style a code block
     */
    public boolean isCodeBlock() {
        return codeBlock;
    }

    /**
     * @return Is this style a spoiler
     */
    public boolean isSpoiler() {
        return spoiler;
    }

    /**
     * Adds two styles together
     *
     * @param style The style to add
     * @return The merged styles
     */
    @NotNull
    public Style add(@NotNull Style style) {
        Style result = new Style(this);

        result.bold |= style.bold;
        result.italic |= style.italic;
        result.underline |= style.underline;
        result.strikethrough |= style.strikethrough;
        result.bigHeader |= style.bigHeader;
        result.smallHeader |= style.smallHeader;
        result.smallerHeader |= style.smallerHeader;
        result.subtext |= style.subtext;
        result.quote |= style.quote;
        result.bulletPoints |= style.bulletPoints;
        result.code |= style.code;
        result.codeBlock |= style.codeBlock;
        result.spoiler |= style.spoiler;

        return result;
    }

    /**
     * Removes one style from another
     *
     * @param style The style to remove
     * @return The divorced styles
     */
    @NotNull
    public Style remove(@NotNull Style style) {
        Style result = new Style(this);

        result.bold &= !style.bold;
        result.italic &= !style.italic;
        result.underline &= !style.underline;
        result.strikethrough &= !style.strikethrough;
        result.bigHeader &= !style.bigHeader;
        result.smallHeader &= !style.smallHeader;
        result.smallerHeader &= !style.smallerHeader;
        result.subtext &= !style.subtext;
        result.quote &= !style.quote;
        result.bulletPoints &= !style.bulletPoints;
        result.code &= !style.code;
        result.codeBlock &= !style.codeBlock;
        result.spoiler &= !style.spoiler;

        return result;
    }

    @Override
    @NotNull
    public String toString() {
        return "Style{" +
                "bold=" + bold +
                ", italic=" + italic +
                ", underline=" + underline +
                ", strikethrough=" + strikethrough +
                ", bigHeader=" + bigHeader +
                ", smallHeader=" + smallHeader +
                ", smallerHeader=" + smallerHeader +
                ", subtext=" + subtext +
                ", quote=" + quote +
                ", bulletPoints=" + bulletPoints +
                ", code=" + code +
                ", codeBlock=" + codeBlock +
                ", spoiler=" + spoiler +
                '}';
    }
}