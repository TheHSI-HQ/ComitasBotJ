package cloud.thehsi.ExamplePlugin;

import cloud.thehsi.ComitasBotJ.API.Discord.Commands.*;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Style;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExampleCommand implements CommandSupplier {
    @SuppressWarnings("unused")
    @Command(name = "example", description = "An example command", commandContextType = {
            CommandContextType.BOT_DM,
            CommandContextType.GUILD
    })
    public void exampleCommand(
            @NotNull CommandRanContext commandRanContext,
            @CommandOption(name = "target", description = "The Target") @NotNull User target,
            @CommandOption(name = "message", description = "The Message", required = false) @Nullable String message
    ) {
        commandRanContext.replyEphemeral(Component.text("Ok, imma tell ").append(Component.text(target.getDisplayName())));

        if (message != null)
            commandRanContext.getChannel().sendMessage(target.mention().append(Component.text(", im supposed to tell you: ")).append(Component.text(message).style(Style.CODE)));
        else
            commandRanContext.getChannel().sendMessage(Component.text("Hi ").append(target.mention()));
    }
}
