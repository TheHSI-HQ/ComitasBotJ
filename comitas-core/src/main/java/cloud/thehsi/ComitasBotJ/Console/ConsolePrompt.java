package cloud.thehsi.ComitasBotJ.Console;

import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.util.List;

public class ConsolePrompt {
    public ConsolePrompt() {
    }

    private final List<String> commands = List.of(
            "stop", "exit", "quit", "die",
            "plugins", "pl",
            "reload", "rl"
    );

    public void run() {
        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).completer((r, l, c) -> {
                for (String cmd : commands)
                    c.add(new Candidate(cmd));
            }).build();

            Thread consoleThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String command = reader.readLine("> ");
                        ConsoleCommandParser.parseCommand(command);
                    } catch (UserInterruptException | EndOfFileException ignored) {
                    }
                }
            });

            consoleThread.setDaemon(true);
            consoleThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}