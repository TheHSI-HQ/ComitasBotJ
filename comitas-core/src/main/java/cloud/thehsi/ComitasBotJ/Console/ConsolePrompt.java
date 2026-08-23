package cloud.thehsi.ComitasBotJ.Console;

import ch.qos.logback.classic.LoggerContext;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Main;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.*;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ConsolePrompt {
    private @NotNull
    static final Logger logger = LoggerFactory.getLogger("Console");
    private @NotNull
    static final Logger stdInLogger = LoggerFactory.getLogger("StdIn.Console");
    private @NotNull
    final InternalConsoleCommandRegistry registry;

    private @NotNull
    final LineReader lineReader;

    public ConsolePrompt(@NotNull InternalConsoleCommandRegistry registry) {
        this.registry = registry;

        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer((r, l, c) -> {
                        for (String cmd : registry.validCommandList())
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
        if (DebugLogging.isBasicEnabled()) DebugLogging.getLogger().debug("Starting console prompt thread");
        Thread consoleThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String input = lineReader.readLine("> ");
                    stdInLogger.info(input);
                    if (input != null && !input.isBlank()) {
                        List<String> parts = new ArrayList<>(List.of(input.split(" ")));

                        String command = parts.removeFirst();
                        String[] args = parts.toArray(new String[0]);

                        if (DebugLogging.isBasicEnabled()) DebugLogging.getLogger().debug("Parsed command: {} with args: {}", command, args);

                        if (!registry.runCommand(command, args))
                            logger.error("Unknown command: {}", command);
                    }
                } catch (UserInterruptException | EndOfFileException ignored) {
                    if (Main.props().strictSafeMode())
                        System.exit(0);
                }
            }
        }, "console-input");

        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    public void writeDirect(@NotNull String line) {
        System.out.println(line);
    }

    /**
     * Called by TuiAppender on every log event
     */
    public void appendLog(@NotNull String line) {
        // printAbove redraws the prompt line beneath automatically
        lineReader.printAbove(line);
    }

    @NotNull
    public LineReader lineReader() {
        return lineReader;
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