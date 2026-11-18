package demo17;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.Role.Role;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Discord.User.User;

import java.util.List;

/**
 * A plugin written against the 0.0.17 API, compiled by the end to end test.
 */
public class Plugin17 {
    public static String[] lastArgs;

    public static String names(User user, Member member, Role role) {
        return user.getLoggableName() + "," + member.getLoggableName() + "," + role.getLoggableName();
    }

    public static int memberCount(Reaction reaction) {
        List<Member> members = reaction.getReactorMemberss();
        return members == null ? -1 : members.size();
    }

    public static ConsoleCommandRegistry.ConsoleCommand command(ConsoleCommandExecutor executor) {
        return new ConsoleCommandRegistry.ConsoleCommand(List.of("a"), null, "description", executor);
    }

    public static class Echo extends ConsoleCommandExecutor {
        @Override
        public void execute(String[] args) {
            lastArgs = args;
        }
    }
}
