package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class InviteConsoleCommand extends ConsoleCommand {
    public InviteConsoleCommand() {
        Comitas.getConsoleCommandRegistry().register(
                this,
                "Generate an invitation link for the bot",
                "invite", "invitation"
        );
    }

    @Override
    public void execute(@NotNull String[] args) {
        switch (args.length) {
            case 0:
                getConsoleLogger().warn("No permissions specified, granting Administator permission (8)");
                getConsoleLogger().info("Invitation Link: {}", Comitas.getBot().generateInvitationLink(Permission.ADMINISTRATOR));
                break;
            case 1:
                try {
                    long permission = Long.parseLong(args[0]);

                    Permission[] permissions = Permission.fromLong(permission);
                    getConsoleLogger().info("Granting permissions: {}",
                            String.join(", ", Arrays.stream(permissions).map(Enum::name).toArray(String[]::new))
                    );

                    //noinspection LoggingSimilarMessage
                    getConsoleLogger().info("Invitation Link: {}", Comitas.getBot().generateInvitationLink(permissions));
                    break;
                } catch (NumberFormatException e) {
                    getConsoleLogger().error("Cannot parse long: '{}'", args[0]);
                }
            default:
                getConsoleLogger().error("Usage: invite [permissions]");
        }
    }
}
