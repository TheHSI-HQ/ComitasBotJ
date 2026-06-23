package cloud.thehsi.ComitasBotJ.API.Console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public abstract class ConsoleCommandExecutor {
    protected Logger getConsoleLogger() {
        return LoggerFactory.getLogger("Console");
    }

    abstract public void execute(String[] args);
}
