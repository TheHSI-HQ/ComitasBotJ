package demo;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.ChannelType;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Commands.Dynamic.CommandArgumentType;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;

import java.util.List;
import java.util.function.Function;

/**
 * A plugin written against the 0.0.16 API, compiled by the end to end test.
 */
public class OldPlugin {
    public static TextChannel defaultChannel(Guild guild) {
        TextChannel channel = guild.getDefaultChannel();
        return channel;
    }

    public static Function<Guild, TextChannel> defaultChannelFunction() {
        return Guild::getDefaultChannel;
    }

    public static int reactorCount(Reaction reaction) {
        List<Member> reactors = reaction.getReactors();
        int count = 0;
        for (Member member : reactors) count++;
        return count;
    }

    public static String[] aliases(ConsoleCommandExecutor executor, String... aliases) {
        ConsoleCommandRegistry.ConsoleCommand command = new ConsoleCommandRegistry.ConsoleCommand(
                aliases.length > 0 ? aliases : new String[]{"fallback"}, null, "description", executor);
        return command.aliases();
    }

    public static Object integerType() {
        return CommandArgumentType.INTEGER;
    }

    public static ConsoleCommandExecutor executor() {
        return new ConsoleCommandExecutor() {
            @Override
            public void execute(String[] args) {
                lastArgs = args;
            }
        };
    }

    public static String[] lastArgs;

    public static String describe(ChannelType type) {
        switch (type) {
            case VOICE:
                return "voice";
            case TEXT:
                return "text";
            default:
                return "other";
        }
    }
}
