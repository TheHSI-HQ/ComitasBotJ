package cloud.thehsi.ComitasBotJ.API.Console;

import java.util.List;

public interface InternalConsoleCommandRegistryImpl {
    void register(ConsoleCommandRegistry.Command command);

    boolean runCommand(String command, String[] args);

    String[] validCommandList();

    List<ConsoleCommandRegistry.Command> registeredCommands();
}
