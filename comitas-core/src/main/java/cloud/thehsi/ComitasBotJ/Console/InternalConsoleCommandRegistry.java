package cloud.thehsi.ComitasBotJ.Console;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Console.InternalConsoleCommandRegistryImpl;

import java.util.ArrayList;
import java.util.List;

public class InternalConsoleCommandRegistry implements InternalConsoleCommandRegistryImpl {
    private final List<ConsoleCommandRegistry.Command> commands = new ArrayList<>();

    private boolean isCommandMeant(String command, ConsoleCommandRegistry.Command consoleCommand) {
        return consoleCommand.aliases().contains(command);
    }

    @Override
    public boolean runCommand(String command, String[] args) {
        for (ConsoleCommandRegistry.Command cmd : commands) {
            if (!isCommandMeant(command, cmd)) continue;

            cmd.consoleCommandExecutor().execute(args);
            return true;
        }

        return false;
    }

    @Override
    public String[] validCommandList() {
        List<String> commandList = new ArrayList<>();

        commands.forEach(cmd -> commandList.addAll(cmd.aliases()));

        return commandList.toArray(new String[0]);
    }

    @Override
    public List<ConsoleCommandRegistry.Command> registeredCommands() {
        return commands.stream().toList();
    }

    @Override
    public void register(ConsoleCommandRegistry.Command command) {
        commands.add(command);
    }
}
