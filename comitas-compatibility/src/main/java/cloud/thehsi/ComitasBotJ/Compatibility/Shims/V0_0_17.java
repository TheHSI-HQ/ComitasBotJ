package cloud.thehsi.ComitasBotJ.Compatibility.Shims;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.GuildChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Reaction.Reaction;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Shims for the breaking changes of API 0.0.17, used by plugins built against 0.0.16.
 * <p>
 * Every method emulates the old API on top of the current one. Plugins can call these classes
 * (the compatibility layer redirects old calls here), so shims must only use the public API.
 */
@SuppressWarnings("unused")
public final class V0_0_17 {
    private V0_0_17() {
    }

    /**
     * {@code TextChannel Guild#getDefaultChannel()}, now returns a {@link GuildChannel}.
     */
    @Nullable
    public static TextChannel guildGetDefaultChannel(@NotNull Guild guild) {
        GuildChannel channel = guild.getDefaultChannel();
        return channel instanceof TextChannel text ? text : null;
    }

    /**
     * {@code List<Member> Reaction#getReactors()}, now returns users.
     */
    @NotNull
    public static List<Member> reactionGetReactors(@NotNull Reaction reaction) {
        List<Member> members = reaction.getReactorMembers();
        return members == null ? List.of() : members;
    }

    /**
     * {@code new ConsoleCommand(String[], Plugin, String, ConsoleCommandExecutor)}, the aliases are a list now.
     */
    @NotNull
    public static ConsoleCommandRegistry.ConsoleCommand newConsoleCommand(@NotNull String[] aliases, @NotNull Plugin plugin,
                                                                         @NotNull String description,
                                                                         @NotNull ConsoleCommandExecutor executor) {
        return new ConsoleCommandRegistry.ConsoleCommand(List.of(aliases), plugin, description, executor, null);
    }

    /**
     * {@code String[] ConsoleCommand#aliases()}, the aliases are a list now.
     */
    @NotNull
    public static String[] consoleCommandAliases(@NotNull ConsoleCommandRegistry.ConsoleCommand command) {
        return command.aliases().toArray(String[]::new);
    }
}
