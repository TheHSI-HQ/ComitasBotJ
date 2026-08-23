package cloud.thehsi.ComitasBotJ.API.Discord.Message.Components;

import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emoji;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class Component {
    private @NotNull
    final List<Component> children = new ArrayList<>();
    private @NotNull Style style = Style.RESET;
    private @NotNull String content = "";

    /**
     * Copy a component.
     */
    public Component(@NotNull Component component) {
        for (Component child : component.children)
            children.add(new Component(child));
        content = component.content;
        style = component.style;
    }

    private Component() {
    }

    /**
     * An empty component
     *
     * @return The empty component.
     */
    @NotNull
    public static Component empty() {
        return new Component();
    }

    /**
     * A newline component
     *
     * @return The newline component.
     */
    @NotNull
    public static Component newLine() {
        Component c = new Component();
        c.content = "\n";
        return c;
    }

    /**
     * Create a raw component (without auto escaping)
     *
     * @param content The raw content
     * @return The raw component.
     */
    @NotNull
    public static Component raw(@Nullable String content) {
        Component c = new Component();
        c.content = content == null ? "null" : content;
        return c;
    }

    /**
     * Create a component from a Timestamp
     *
     * @param timestamp The timestamp
     * @param format The timestamp format
     * @return The timestamp component.
     */
    @NotNull
    public static Component timestamp(@NotNull Instant timestamp, @NotNull TimeStampFormat format) {
        Component c = new Component();
        c.content = "<t:" + timestamp.getEpochSecond() + ":" + format.getIdentifier() + ">";
        return c;
    }

    /**
     * Create a component from a link
     *
     * @param link The link url
     * @return The link component.
     */
    @NotNull
    public static Component link(@NotNull String link) {
        Component c = new Component();
        c.content = link;
        return c;
    }

    /**
     * Create a component from a link
     *
     * @param link  The link url
     * @param label The link label
     * @return The link component.
     */
    @NotNull
    public static Component link(@NotNull String link, @Nullable String label) {
        Component c = new Component();
        c.content = "[%s](%s)".formatted(label, link);
        return c;
    }

    /**
     * Create a component from text
     *
     * @param content The content
     * @return The text component.
     * @implNote Any content will be auto escaped
     */
    @NotNull
    public static Component text(@Nullable String content) {
        Component c = new Component();
        c.content(content);
        return c;
    }

    /**
     * Create a component from text and multiple styles
     *
     * @param content The content
     * @param styles  The styles
     * @return The text component.
     * @implNote Any content will be auto escaped
     * @implNote Styles will be added together.
     */
    @NotNull
    public static Component text(@Nullable String content, @NotNull Style... styles) {
        Component c = new Component();
        c.content(content);
        Style finalStyle = Style.RESET;
        for (Style style : styles)
            finalStyle = finalStyle.add(style);
        c.style = finalStyle;
        return c;
    }

    /**
     * Converts the component into {@link MessageData}
     */
    @NotNull
    public MessageData asMessageData() {
        return new MessageData(this);
    }

    /**
     * Returns a list of children of this Component.
     *
     * @return A children list of this component.
     */
    @NotNull
    public List<Component> children() {
        return new ArrayList<>(children);
    }

    /**
     * Returns the components style.
     *
     * @return The component style.
     */
    @NotNull
    public Style style() {
        return style;
    }

    /**
     * Returns the components content.
     *
     * @return The component content.
     */
    @NotNull
    public String content() {
        return content;
    }

    /**
     * Sets the components content.
     *
     * @param content The new content
     * @return The component.
     * @implNote Any content will be auto escaped
     */
    @NotNull
    public Component content(@Nullable String content) {
        this.content = content == null ? "null" : content;
        this.content = this.content.replace("\\", "\\\\");
        this.content = this.content.replace("*", "\\*");
        this.content = this.content.replace("_", "\\_");
        this.content = this.content.replace("#", "\\#");
        this.content = this.content.replace("-", "\\-");
        this.content = this.content.replace("~", "\\~");
        this.content = this.content.replace("|", "\\|");
        this.content = this.content.replace("`", "\\`");
        this.content = this.content.replace("<", "\\<");
        this.content = this.content.replace(">", "\\>");
        this.content = this.content.replace("@", "\\@");
        this.content = this.content.replace("[", "\\[");
        this.content = this.content.replace("]", "\\]");
        this.content = this.content.replace("(", "\\(");
        this.content = this.content.replace(")", "\\)");
        return this;
    }

    /**
     * Sets the components raw content.
     *
     * @param content The new raw content
     * @return The component.
     */
    @NotNull
    public Component rawContent(@NotNull String content) {
        this.content = content;
        return this;
    }

    /**
     * Sets the components style.
     *
     * @param style The new style
     * @return The component.
     */
    @NotNull
    public Component style(@Nullable Style style) {
        if (style == null) style = Style.RESET;
        this.style = style;
        return this;
    }

    /**
     * Append an emoji to this component.
     *
     * @param emoji Emoji to be appended
     * @return The component.
     */
    @NotNull
    public Component append(@NotNull Emoji emoji) {
        this.children.add(emoji.asComponent());
        return this;
    }

    /**
     * Append a string to this component.
     *
     * @param child String to be appended
     * @return The component.
     */
    @NotNull
    public Component append(@Nullable String child) {
        this.children.add(Component.text(child));
        return this;
    }

    /**
     * Append components to this component.
     *
     * @param children Components to be appended
     * @return The component.
     */
    @NotNull
    public Component append(@NotNull Component... children) {
        for (Component child : children)
            this.append(child);
        return this;
    }

    @Override
    @NotNull
    public String toString() {
        return "Component{" +
                "children=" + Arrays.deepToString(children.toArray()) +
                ", style=" + style +
                ", content='" + content + '\'' +
                '}';
    }
}
