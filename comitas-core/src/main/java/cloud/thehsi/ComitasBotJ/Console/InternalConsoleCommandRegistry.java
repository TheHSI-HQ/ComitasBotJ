package cloud.thehsi.ComitasBotJ.Console;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Main;
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
                cmd.consoleCommandExecutor().execute(args);
            } catch (Exception e) {
                logger.error("Failed to execute command '{}'", command, e);
            }
            return true;
        }

        return false;
    }

    @NotNull
    public String[] validCommandList() {
        List<String> commandList = new ArrayList<>();

        consoleCommands.forEach(cmd -> commandList.addAll(cmd.aliases()));

        return commandList.toArray(new String[0]);
    }

    @Override
    public void register(@NotNull ConsoleCommandExecutor executor, @Nullable String description, @NotNull String... aliases) {
        DebugLogging.action(executor, description, aliases);
        consoleCommands.add(
                new ConsoleCommand(List.of(aliases), Comitas.getPluginManager().getPlugin(), description == null ? "" : description, executor)
        );
    }

    /**
     * Removes all console commands, except those owned by the given plugin.
     * Used during reloads, so executors of unloaded plugins aren't kept around.
     */
    public void unregisterAllExcept(@Nullable Plugin keep) {
        consoleCommands.removeIf(cmd -> cmd.plugin() != keep);
    }

    @Override
    public @NotNull List<ConsoleCommand> registeredCommands() {
        DebugLogging.action();
        return new ArrayList<>(consoleCommands);
    }
}
