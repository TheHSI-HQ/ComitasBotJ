package cloud.thehsi.ComitasBotJ.API.Console;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public abstract class ConsoleCommandExecutor {
    @NotNull
    protected Logger getConsoleLogger() {
        return LoggerFactory.getLogger("Console");
    }

    abstract public void execute(@NotNull String[] args);
}
