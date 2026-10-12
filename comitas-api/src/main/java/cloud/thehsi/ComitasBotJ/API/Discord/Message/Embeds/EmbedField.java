package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import org.jetbrains.annotations.NotNull;

/**
 * An embed field
 */
@SuppressWarnings("unused")
public class EmbedField {
    @NotNull
    final Component name;
    @NotNull
    final Component value;
    boolean inline = true;

    /**
     * Create an embed field from a name and value
     *
     * @param name  The field name as a component
     * @param value The field value as a component
     */
    public EmbedField(@NotNull Component name, @NotNull Component value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Create an embed field from a name and value
     *
     * @param name The field name as a component
     * @param value The field value as a component
     * @param inline Is this field inlíne
     */
    public EmbedField(@NotNull Component name, @NotNull Component value, boolean inline) {
        this.name = name;
        this.value = value;
        this.inline = inline;
    }

    /**
     * @return The fields name
     */
    @NotNull
    public Component getName() {
        return name;
    }

    /**
     * @return The fields value
     */
    @NotNull
    public Component getValue() {
        return value;
    }

    /**
     * @return Is the field inline
     */
    @NotNull
    public Boolean isInline() {
        return inline;
    }

    @Override
    @NotNull
    public String toString() {
        return "EmbedField{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", inline=" + inline +
                '}';
    }
}
