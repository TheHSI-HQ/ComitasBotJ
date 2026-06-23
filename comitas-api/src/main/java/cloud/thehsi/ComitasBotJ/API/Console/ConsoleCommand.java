package cloud.thehsi.ComitasBotJ.API.Console;

@SuppressWarnings("unused")
public abstract class ConsoleCommand extends ConsoleCommandExecutor {
    public ConsoleCommand() {
    }

    abstract public void execute(String[] args);
}
