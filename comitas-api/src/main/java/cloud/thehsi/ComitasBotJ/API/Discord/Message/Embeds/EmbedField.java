package cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;

@SuppressWarnings("unused")
public class EmbedField {
    final Component name;
    final Component value;
    boolean inline = true;

    public EmbedField(Component name, Component value) {
        this.name = name;
        this.value = value;
    }

    public EmbedField(Component name, Component value, boolean inline) {
        this.name = name;
        this.value = value;
        this.inline = inline;
    }

    public Component getName() {
        return name;
    }

    public Component getValue() {
        return value;
    }

    public Boolean isInline() {
        return inline;
    }

    @Override
    public String toString() {
        return "EmbedField{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", inline=" + inline +
                '}';
    }
}
