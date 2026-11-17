package cloud.thehsi.ExamplePlugin;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.*;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Style;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataStorage;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Subcommands: <code>/exampleplugin ping</code> and <code>/exampleplugin message get|set</code>.
 * <p>
 * The parent is declared on the class, every subcommand is a normal {@link Command} with a parent (and group).
 * Discord only knows settings like permissions on the parent, a parent itself can't be run.
 */
@CommandParent(
        name = "exampleplugin",
        description = "Commands of the example plugin",
        groups = @CommandGroup(name = "message", description = "The stored message")
)
public class ExampleSubcommands implements CommandSupplier {
    @SuppressWarnings("unused")
    @Command(parent = "exampleplugin", name = "ping", description = "Replies with pong")
    public void ping(@NotNull CommandRanContext ctx) {
        ctx.replyEphemeral(Component.text("Pong from /" + ctx.getCommandName() + " " + ctx.getSubcommandName()));
    }

    @SuppressWarnings("unused")
    @Command(parent = "exampleplugin", group = "message", name = "get", description = "Shows the stored message")
    public void getMessage(@NotNull CommandRanContext ctx) {
        PersistentDataStorage storage = Comitas.getPluginManager().getPersistentDataStorage();

        if (storage.has("message", PersistentDataTypes.STRING))
            ctx.reply(Component.text("message is: ").append(Component.text(storage.get("message", PersistentDataTypes.STRING), Style.CODE)));
        else
            ctx.replyEphemeral(Component.text("No message stored yet"));
    }

    @SuppressWarnings("unused")
    @Command(parent = "exampleplugin", group = "message", name = "set", description = "Stores a message")
    public void setMessage(
            @NotNull CommandRanContext ctx,
            @CommandOption(name = "value", description = "The message to store") @NotNull String value
    ) {
        Comitas.getPluginManager().getPersistentDataStorage().set("message", PersistentDataTypes.STRING, value);
        ctx.reply(Component.text("Set ").append(Component.text("message", Style.CODE)).append(" to ").append(Component.text(value, Style.CODE)));
    }
}
