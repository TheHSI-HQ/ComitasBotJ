package cloud.thehsi.ComitasBotJ.Console;

import ch.qos.logback.classic.LoggerContext;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ConsolePrompt {

    private static final List<String> COMMANDS = List.of(
            "stop", "exit", "quit", "die",
            "plugins", "pl",
            "reload", "rl"
    );

    private final LineReader lineReader;

    public ConsolePrompt() {
        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer((r, l, c) -> {
                        for (String cmd : COMMANDS)
                            c.add(new Candidate(cmd));
                    })
                    .build();

            // Tell JLine not to print logs directly to the terminal —
            // we'll use printAbove() so it redraws the prompt correctly
            lineReader.setOpt(LineReader.Option.AUTO_FRESH_LINE);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize terminal", e);
        }

        registerLogAppender();
    }

    public void run() {
        Thread consoleThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String command = lineReader.readLine("> ");
                    if (command != null && !command.isBlank()) {
                        ConsoleCommandParser.parseCommand(command);
                    }
                } catch (UserInterruptException | EndOfFileException ignored) {
                }
            }
        }, "console-input");

        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    /**
     * Called by TuiAppender on every log event
     */
    public void appendLog(String line) {
        if (lineReader == null) return;
        // printAbove redraws the prompt line beneath automatically
        lineReader.printAbove(line);
    }

    private void registerLogAppender() {
        LoggerContext ctx =
                (LoggerContext) LoggerFactory.getILoggerFactory();

        ch.qos.logback.classic.Logger root =
                ctx.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        // Detach anything that isn't the file appender (removes any console appender)
        root.iteratorForAppenders().forEachRemaining(appender -> {
            if (!appender.getName().equals("FILE")) {
                root.detachAppender(appender);
            }
        });

        TuiAppender.setConsolePrompt(this);

        TuiAppender tuiAppender = new TuiAppender(ctx);
        tuiAppender.setName("TUI");
        tuiAppender.start();

        root.addAppender(tuiAppender);
    }
}