package cloud.thehsi.ExamplePlugin;

import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Command;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandOption;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.CommandSupplier;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Context;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.Nullable;

public class ExampleCommand implements CommandSupplier {
    @Command(name = "example", description = "An example command")
    public void exampleCommand(
            Context context,
            @CommandOption(name = "target", description = "The Target") Member target,
            @CommandOption(name = "message", description = "The Message", required = false) @Nullable String message
    ) {
        context.channel().sendMessage(target.mention().append(Component.text(" ")).append(Component.text(message)));
    }
}
