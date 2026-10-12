package cloud.thehsi.ComitasBotJ.API.Console;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class used to create console commands
 */
@SuppressWarnings("unused")
public abstract class ConsoleCommandExecutor {
    @NotNull
    protected Logger getConsoleLogger() {
        return LoggerFactory.getLogger("Console");
    }

    /**
     * Execute the console command with an array of arguments
     *
     * @param args Array of arguments
     */
    abstract public void execute(@NotNull String[] args);
}
