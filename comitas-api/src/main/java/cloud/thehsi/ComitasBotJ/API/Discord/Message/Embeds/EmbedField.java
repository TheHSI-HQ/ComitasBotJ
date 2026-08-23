package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class EmbedField {
    @NotNull
    final Component name;
    @NotNull
    final Component value;
    boolean inline = true;

    public EmbedField(@NotNull Component name, @NotNull Component value) {
        this.name = name;
        this.value = value;
    }

    public EmbedField(@NotNull Component name, @NotNull Component value, boolean inline) {
        this.name = name;
        this.value = value;
        this.inline = inline;
    }

    @NotNull
    public Component getName() {
        return name;
    }

    @NotNull
    public Component getValue() {
        return value;
    }

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
