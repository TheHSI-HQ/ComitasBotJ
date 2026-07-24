package cloud.thehsi.ComitasBotJ.API.Discord.Message.Components;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Component {
    private final List<Component> children = new ArrayList<>();
    private Style style = Style.RESET;
    private String content = "";

    private Component() {}

    public Component(Component component) {
        for (Component child : component.children)
            children.add(new Component(child));
        content = component.content;
        style = component.style;
    }

    public List<Component> children() {
        return new ArrayList<>(children);
    }

    public Style style() {
        return style;
    }

    public String content() {
        return content;
    }

    public Component content(String content) {
        this.content = content;
        this.content = this.content.replace("\\", "\\\\");
        this.content = this.content.replace("*", "\\*");
        this.content = this.content.replace("_", "\\_");
        this.content = this.content.replace("#", "\\#");
        this.content = this.content.replace("-", "\\-");
        this.content = this.content.replace("~", "\\~");
        this.content = this.content.replace("|", "\\|");
        this.content = this.content.replace("`", "\\`");
        this.content = this.content.replace(">", "\\>");
        return this;
    }

    public Component style(Style style) {
        if (style == null) style = Style.RESET;
        this.style = style;
        return this;
    }

    public Component append(Component child) {
        this.children.add(child);
        this.children.addAll(child.children);
        child.children.clear();
        return this;
    }

    public Component append(Component... children) {
        for (Component child : children)
            this.append(child);
        return this;
    }

    public static Component empty() {
        return new Component();
    }

    public static Component raw(String content) {
        Component c = new Component();
        c.content = content;
        return c;
    }

    public static Component link(String link) {
        Component c = new Component();
        c.content = link;
        return c;
    }

    public static Component link(String link, String label) {
        Component c = new Component();
        c.content = "[%s](%s)".formatted(link, label);
        return c;
    }

    public static Component text(String content) {
        Component c = new Component();
        c.content(content);
        return c;
    }

    public static Component text(String content, Style style) {
        Component c = new Component();
        c.content(content);
        c.style(style);
        return c;
    }

    public static Component text(String content, Style... styles) {
        Component c = new Component();
        c.content = content;
        Style finalStyle = Style.RESET;
        for (Style style : styles)
            finalStyle = finalStyle.add(style);
        c.style = finalStyle;
        return c;
    }
}
