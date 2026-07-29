package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;

public class InviteConsoleCommand extends ConsoleCommand {
    public InviteConsoleCommand() {
        Comitas.getConsoleCommandRegistry().register(
                this,
                "Generate an invitation link for the bot",
                "invite", "invitation"
        );
    }

    @Override
    public void execute(String[] args) {
        getConsoleLogger().info("Invitation Link: {}", Comitas.getBot().generateInvitationLink());
    }
}
