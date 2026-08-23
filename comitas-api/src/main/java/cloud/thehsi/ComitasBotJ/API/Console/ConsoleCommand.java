package cloud.thehsi.ComitasBotJ.API.Console;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public abstract class ConsoleCommand extends ConsoleCommandExecutor {
    public ConsoleCommand() {
    }

    abstract public void execute(@NotNull String[] args);
}
