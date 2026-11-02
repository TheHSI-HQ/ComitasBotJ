package cloud.thehsi.ComitasBotJ.Console;

import ch.qos.logback.classic.LoggerContext;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Main;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ConsolePrompt {
    private @NotNull
    static final Logger logger = LoggerFactory.getLogger("Console");
    private @NotNull
    static final Logger stdInLogger = LoggerFactory.getLogger("StdIn.Console");
    private @NotNull
    final InternalConsoleCommandRegistry registry;

    private @NotNull
    final LineReader lineReader;
    private @NotNull
    final Deque<Question> questions = new ArrayDeque<>();
    private static volatile @Nullable Thread consoleThread;
    /**
     * True while a question is read directly (see {@link #ask(String, String)}), it isn't a command
     */
    private volatile boolean answeringDirectly;

    public ConsolePrompt(@NotNull InternalConsoleCommandRegistry registry) {
        this.registry = registry;

        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .parser(createParser())
                    .completer(this::complete)
                    .build();

            // Tell JLine not to print logs directly to the terminal —
            // we'll use printAbove() so it redraws the prompt correctly
            lineReader.setOpt(LineReader.Option.AUTO_FRESH_LINE);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize terminal", e);
        }

        registerLogAppender();
    }

    /**
     * Completes the command name for the first word, otherwise asks the command's tab completer.
     * Answers to questions (permission prompts, ...) aren't completed.
     */
    private void complete(@NotNull LineReader reader, @NotNull ParsedLine line, @NotNull List<Candidate> candidates) {
        if (answeringDirectly) return;
        synchronized (questions) {
            if (!questions.isEmpty()) return;
        }

        for (String suggestion : registry.complete(wordsBeforeCursor(line)))
            candidates.add(new Candidate(suggestion));
    }

    /**
     * Plain whitespace separated words, like commands are run. Quotes and backslashes have no meaning,
     * an unclosed quote would otherwise make JLine wait for more lines.
     */
    @NotNull
    static Parser createParser() {
        return new DefaultParser()
                .quoteChars(new char[0])
                .escapeChars(new char[0])
                .eofOnUnclosedQuote(false);
    }

    /**
     * @return The words left of the cursor, the last one is the word being completed, cut at the cursor (may be empty)
     */
    @NotNull
    static List<String> wordsBeforeCursor(@NotNull ParsedLine line) {
        int index = line.wordIndex();
        if (index < 0) return List.of("");
        List<String> words = new ArrayList<>(line.words().subList(0, index));
        words.add(line.word().substring(0, Math.min(line.wordCursor(), line.word().length())));
        return words;
    }

    /**
     * @return If the current thread reads the console input, e.g. while running a console command
     */
    public static boolean isConsoleThread() {
        return Thread.currentThread() == consoleThread;
    }

    /**
     * Asks the person at the console a question.
     * <p>
     * Before the console thread runs, or when called from it (e.g. by a console command), the answer is read directly.
     * Otherwise the question is shown above the prompt and the next entered line answers it, instead of being run as a command.
     *
     * @param message Shown before asking, may span multiple lines
     * @param prompt  The prompt, the answer is entered after
     * @return The answer, or null if nothing can be read anymore
     */
    @NotNull
    public CompletableFuture<@Nullable String> ask(@NotNull String message, @NotNull String prompt) {
        Thread thread = consoleThread;
        if (thread == null || Thread.currentThread() == thread) {
            synchronized (this) {
                answeringDirectly = true;
                try {
                    lineReader.getTerminal().writer().println(message);
                    lineReader.getTerminal().writer().flush();
                    return CompletableFuture.completedFuture(lineReader.readLine(prompt));
                } catch (UserInterruptException e) {
                    return CompletableFuture.completedFuture("");
                } catch (EndOfFileException | IllegalStateException e) {
                    return CompletableFuture.completedFuture(null);
                } finally {
                    answeringDirectly = false;
                }
            }
        }

        Question question = new Question(message, prompt, new CompletableFuture<>());
        synchronized (questions) {
            questions.add(question);
            if (questions.peek() == question) show(question);
        }
        return question.answer();
    }

    private void show(@NotNull Question question) {
        lineReader.printAbove(question.message());
        try {
            if (lineReader instanceof LineReaderImpl impl) impl.setPrompt(question.prompt());
            lineReader.callWidget(LineReader.REDRAW_LINE);
            lineReader.callWidget(LineReader.REDISPLAY);
        } catch (IllegalStateException ignored) {
            // Not reading right now, the prompt is used on the next read
        }
    }

    public void run() {
        if (DebugLogging.isBasicEnabled()) DebugLogging.getLogger().debug("Starting console prompt thread");
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Question pending;
                    synchronized (questions) {
                        pending = questions.peek();
                    }
                    String input = lineReader.readLine(pending == null ? "> " : pending.prompt());

                    synchronized (questions) {
                        pending = questions.poll();
                        if (pending != null) {
                            Question next = questions.peek();
                            if (next != null) show(next);
                        }
                    }
                    if (pending != null) {
                        pending.answer().complete(input);
                        continue;
                    }

                    stdInLogger.info(input);
                    if (input != null && !input.isBlank()) {
                        // Split like the completion parser does, so repeated spaces don't create empty arguments
                        List<String> parts = new ArrayList<>(List.of(input.strip().split("\\s+")));

                        String command = parts.removeFirst();
                        String[] args = parts.toArray(new String[0]);

                        if (DebugLogging.isBasicEnabled()) DebugLogging.getLogger().debug("Parsed command: {} with args: {}", command, args);

                        if (!registry.runCommand(command, args))
                            logger.error("Unknown command: {}", command);
                    }
                } catch (UserInterruptException ignored) {
                    if (Main.props().strictSafeMode())
                        System.exit(0);
                } catch (EndOfFileException e) {
                    // The input was closed, reading again would fail right away and spin
                    logger.warn("The console input was closed, console commands aren't available anymore");
                    if (Main.props().strictSafeMode())
                        System.exit(0);
                    break;
                }
            }

            // Nobody can answer anymore
            consoleThread = null;
            synchronized (questions) {
                for (Question question : questions) question.answer().complete(null);
                questions.clear();
            }
        }, "console-input");

        thread.setDaemon(true);
        consoleThread = thread;
        thread.start();
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

    private record Question(@NotNull String message, @NotNull String prompt,
                            @NotNull CompletableFuture<@Nullable String> answer) {
    }
}