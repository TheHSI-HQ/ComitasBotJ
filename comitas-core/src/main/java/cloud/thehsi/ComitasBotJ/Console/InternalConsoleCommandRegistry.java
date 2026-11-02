package cloud.thehsi.ComitasBotJ.Console;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandTabCompleter;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Main;
import cloud.thehsi.ComitasBotJ.Utility.ConsoleText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InternalConsoleCommandRegistry implements ConsoleCommandRegistry {
    private @NotNull
    final List<ConsoleCommand> consoleCommands = new CopyOnWriteArrayList<>();
    private @NotNull
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".ConsoleCommandRegistry");

    private boolean isCommandMeant(@NotNull String command, @NotNull ConsoleCommand consoleCommand) {
        return consoleCommand.aliases().contains(command);
    }

    public boolean runCommand(@NotNull String command, @NotNull String @NotNull [] args) {
        for (ConsoleCommand cmd : consoleCommands) {
            if (!isCommandMeant(command, cmd)) continue;

            try {
                cmd.consoleCommandExecutor().execute(args, command, cmd);
            } catch (Exception e) {
                logger.error("Failed to execute command '{}'", command, e);
            }
            return true;
        }

        return false;
    }

    @Override
    public void register(@NotNull ConsoleCommandExecutor executor, @Nullable String description, @NotNull String... aliases) {
        register(executor, executor instanceof ConsoleCommandTabCompleter completer ? completer : null, description, aliases);
    }

    /**
     * Completes a console line: the command name for the first word, otherwise the command's arguments.
     *
     * @param words The words of the line up to the cursor, the last one is the word being completed (may be empty)
     * @return The suggestions, without escape sequences
     */
    @NotNull
    public List<String> complete(@NotNull List<String> words) {
        if (words.size() <= 1) return validCommandNames();

        String alias = words.getFirst();
        for (ConsoleCommand cmd : consoleCommands) {
            if (!isCommandMeant(alias, cmd)) continue;

            ConsoleCommandTabCompleter completer = cmd.consoleCommandTabCompleter();
            if (completer == null) return List.of();

            String[] args = words.subList(1, words.size()).toArray(String[]::new);
            String[] suggestions;
            try {
                suggestions = completer.complete(args, alias, cmd);
            } catch (Exception | LinkageError e) {
                if (DebugLogging.isBasicEnabled())
                    DebugLogging.getLogger().debug("Tab completion of '{}' failed", alias, e);
                return List.of();
            }

            //noinspection ConstantValue - plugins may still return null
            if (suggestions == null) return List.of();
            List<String> result = new ArrayList<>();
            for (String suggestion : suggestions) {
                // Comes from plugins, it must not be able to write escape sequences to the terminal
                String plain = suggestion == null ? "" : ConsoleText.plain(suggestion).strip();
                if (!plain.isEmpty() && !result.contains(plain)) result.add(plain);
            }
            return result;
        }
        return List.of();
    }

    @NotNull
    private List<String> validCommandNames() {
        List<String> names = new ArrayList<>();
        for (ConsoleCommand cmd : consoleCommands)
            for (String alias : cmd.aliases())
                if (!names.contains(alias)) names.add(alias);
        return names;
    }

    @Override
    public void register(@NotNull ConsoleCommandExecutor executor, @Nullable ConsoleCommandTabCompleter tabCompleter, @Nullable String description, @NotNull String... aliases) {
        DebugLogging.action(executor, description, aliases);
        add(new ConsoleCommand(List.of(aliases), Comitas.getPluginManager().getPlugin(), description == null ? "" : description, executor, tabCompleter));
    }

    void add(@NotNull ConsoleCommand command) {
        consoleCommands.add(command);
    }

    /**
     * Removes all console commands, except those owned by the given plugin.
     * Used during reloads, so executors of unloaded plugins aren't kept around.
     */
    public void unregisterAllExcept(@Nullable Plugin keep) {
        consoleCommands.removeIf(cmd -> cmd.plugin() != keep);
    }

    /**
     * Removes the console commands of one plugin.
     */
    public void unregisterAll(@NotNull Plugin plugin) {
        consoleCommands.removeIf(cmd -> cmd.plugin() == plugin);
    }

    @Override
    public @NotNull List<ConsoleCommand> registeredCommands() {
        DebugLogging.action();
        return new ArrayList<>(consoleCommands);
    }
}
