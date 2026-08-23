package cloud.thehsi.ComitasBotJ.Console;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class InternalConsoleCommandRegistry implements ConsoleCommandRegistry {
    private @NotNull
    final List<ConsoleCommand> consoleCommands = new ArrayList<>();
    private @NotNull
    final Logger logger = LoggerFactory.getLogger(Main.LOGGER_ROOT_PATH + ".ConsoleCommandRegistry");

    private boolean isCommandMeant(@NotNull String command, @NotNull ConsoleCommand consoleCommand) {
        return List.of(consoleCommand.aliases()).contains(command);
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

        consoleCommands.forEach(cmd -> commandList.addAll(List.of(cmd.aliases())));

        return commandList.toArray(new String[0]);
    }

    @Override
    public void register(@NotNull ConsoleCommandExecutor executor, @Nullable String description, @NotNull String... aliases) {
        DebugLogging.action(executor, description, aliases);
        consoleCommands.add(
                new ConsoleCommand(aliases, Comitas.getPluginManager().getPlugin(), description, executor)
        );
    }

    @Override
    public @NotNull List<ConsoleCommand> registeredCommands() {
        DebugLogging.action();
        return new ArrayList<>(consoleCommands);
    }
}
