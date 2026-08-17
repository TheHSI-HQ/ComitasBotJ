package cloud.thehsi.ComitasBotJ.API.Discord.Message.Components;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Component {
    private final List<Component> children = new ArrayList<>();
    private Style style = Style.RESET;
    private String content = "";

    public MessageData toMessageData() {
        return new MessageData(this);
    }

    private Component() {
    }

    /**
     * Copy a component.
     */
    public Component(Component component) {
        for (Component child : component.children)
            children.add(new Component(child));
        content = component.content;
        style = component.style;
    }

    /**
     * An empty component
     *
     * @return The empty component.
     */
    public static Component empty() {
        return new Component();
    }

    /**
     * A newline component
     *
     * @return The newline component.
     */
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
    public static Component raw(String content) {
        Component c = new Component();
        c.content = content;
        return c;
    }

    /**
     * Create a component from a Timestamp
     *
     * @param timestamp The timestamp
     * @param format The timestamp format
     * @return The timestamp component.
     */
    public static Component timestamp(Instant timestamp, TimeStampFormat format) {
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
    public static Component link(String link) {
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
    public static Component link(String link, String label) {
        Component c = new Component();
        c.content = "[%s](%s)".formatted(link, label);
        return c;
    }

    /**
     * Create a component from text
     *
     * @param content The content
     * @return The text component.
     * @implNote Any content will be auto escaped
     */
    public static Component text(String content) {
        Component c = new Component();
        c.content(content);
        return c;
    }

    /**
     * Create a component from text and a style
     *
     * @param content The content
     * @param style   The style
     * @return The text component.
     * @implNote Any content will be auto escaped
     */
    public static Component text(String content, Style style) {
        Component c = new Component();
        c.content(content);
        c.style(style);
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
    public static Component text(String content, Style... styles) {
        Component c = new Component();
        c.content = content;
        Style finalStyle = Style.RESET;
        for (Style style : styles)
            finalStyle = finalStyle.add(style);
        c.style = finalStyle;
        return c;
    }

    /**
     * Returns a list of children of this Component.
     *
     * @return A children list of this component.
     */
    public List<Component> children() {
        return new ArrayList<>(children);
    }

    /**
     * Returns the components style.
     *
     * @return The component style.
     */
    public Style style() {
        return style;
    }

    /**
     * Returns the components content.
     *
     * @return The component content.
     */
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
    public Component content(String content) {
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
    public Component rawContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * Sets the components style.
     *
     * @param style The new style
     * @return The component.
     */
    public Component style(Style style) {
        if (style == null) style = Style.RESET;
        this.style = style;
        return this;
    }

    /**
     * Append a string to this component.
     *
     * @param child String to be appended
     * @return The component.
     */
    public Component append(String child) {
        this.children.add(Component.text(child));
        return this;
    }

    /**
     * Append a component to this component.
     *
     * @param child Component to be appended
     * @return The component.
     */
    public Component append(Component child) {
        this.children.add(child);
        this.children.addAll(child.children);
        child.children.clear();
        return this;
    }

    /**
     * Append components to this component.
     *
     * @param children Components to be appended
     * @return The component.
     */
    public Component append(Component... children) {
        for (Component child : children)
            this.append(child);
        return this;
    }
}
