package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

public class InviteConsoleCommand extends ConsoleCommand {
    public InviteConsoleCommand(Plugin plugin) {
        plugin.createCommandBuilder(this)
                .setDescription("Generate an invitation link for the bot")
                .addCommand("invite")
                .addCommand("invitation")
                .register();
    }

    @Override
    public void execute(String[] args) {
        getConsoleLogger().info("Invitation Link: {}", Comitas.getBot().generateInvitationLink());
    }
}
