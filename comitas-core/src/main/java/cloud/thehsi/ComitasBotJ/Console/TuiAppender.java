package cloud.thehsi.ComitasBotJ.Console;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import cloud.thehsi.ComitasBotJ.Utility.ConsoleText;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TuiAppender extends AppenderBase<ILoggingEvent> {
    // ANSI color codes
    private @NotNull
    static final String RESET = "\033[0m";
    private @NotNull
    static final String GREY = "\033[90m"; // timestamp brackets
    private @NotNull
    static final String WHITE = "\033[97m"; // logger name
    private @NotNull
    static final String BOLD = "\033[1m";
    private @NotNull
    static final String INFO = "\033[92m"; // bright green
    private @NotNull
    static final String WARN = "\033[93m"; // bright yellow
    private @NotNull
    static final String ERROR = "\033[91m"; // bright red
    private @NotNull
    static final String DEBUG = "\033[94m"; // bright blue
    private @NotNull
    static final String TRACE = "\033[90m"; // grey
    private @Nullable
    static ConsolePrompt instance = null;
    private static volatile boolean bypass = false;

    public TuiAppender(@NotNull Context context) {
        setContext(context);
    }

    public static void setConsolePrompt(@NotNull ConsolePrompt prompt) {
        instance = prompt;
    }

    public static void setBypassMode(boolean b) {
        bypass = b;
    }

    @NotNull
    public static String formatThrowable(@NotNull IThrowableProxy throwable) {
        StringBuilder sb = new StringBuilder();

        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss")
                .format(new java.util.Date());
        appendThrowable(sb, throwable, timestamp, "", 0);
        return sb.toString();
    }

    private static void appendThrowable(@NotNull StringBuilder sb, @NotNull IThrowableProxy throwable,
                                        @NotNull String timestamp, @NotNull String prefix, int depth) {
        String linePrefix = GREY + "[" + RESET + DEBUG + timestamp + RESET + GREY + "] " + ERROR;

        sb.append(linePrefix)
                .append(prefix)
                .append(throwable.getClassName())
                .append(": ")
                .append(ConsoleText.forTerminal(throwable.getMessage()))
                .append('\n');

        StackTraceElementProxy[] steps = throwable.getStackTraceElementProxyArray();
        int shown = steps.length - throwable.getCommonFrames();
        for (int i = 0; i < shown; i++)
            sb.append(linePrefix).append('\t').append(steps[i].getSTEAsString()).append('\n');
        if (throwable.getCommonFrames() > 0)
            sb.append(linePrefix).append("\t... ").append(throwable.getCommonFrames()).append(" more\n");

        // Guard against cyclic causes
        if (depth >= 16) return;
        for (IThrowableProxy suppressed : throwable.getSuppressed())
            appendThrowable(sb, suppressed, timestamp, CoreConstants.SUPPRESSED, depth + 1);
        if (throwable.getCause() != null)
            appendThrowable(sb, throwable.getCause(), timestamp, CoreConstants.CAUSED_BY, depth + 1);
    }

    @Override
    protected void append(@NotNull ILoggingEvent event) {
        if (instance == null) return;
        if (event.getLoggerName().startsWith("StdIn.")) return; // Log those only to log files

        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss")
                .format(new java.util.Date(event.getTimeStamp()));

        String levelColor = switch (event.getLevel().toInt()) {
            case Level.INFO_INT -> INFO;
            case Level.WARN_INT -> WARN;
            case Level.ERROR_INT -> ERROR;
            case Level.DEBUG_INT -> DEBUG;
            case Level.TRACE_INT -> TRACE;
            default -> WHITE;
        };

        String levelStr = event.getLevel().toString();

        String logger = abbreviateLogger(event.getLoggerName());

        String formatted = GREY + "[" + RESET
                + DEBUG + timestamp + RESET
                + GREY + "] [" + RESET
                + WHITE + logger + RESET
                + GREY + "/" + RESET
                + levelColor + BOLD + levelStr + RESET
                + GREY + "]: " + RESET
                // Plugins and Discord users control parts of log messages, only colors are passed through
                + ConsoleText.forTerminal(event.getFormattedMessage()) + RESET;

        if (bypass) {
            instance.writeDirect(formatted);
        } else {
            instance.appendLog(formatted);
        }

        IThrowableProxy throwable = event.getThrowableProxy();
        if (throwable != null) {
            if (bypass) instance.writeDirect(formatThrowable(throwable));
            else instance.appendLog(formatThrowable(throwable));
        }
    }

    @NotNull
    private String abbreviateLogger(@Nullable String name) {
        if (name == null) return WHITE + "?";

        String[] parts = name.split("\\.");
        if (parts.length <= 1) return WHITE + name;

        StringBuilder sb = new StringBuilder(GREY);
        for (int i = 0; i < parts.length - 1; i++) {
            sb.append(parts[i]).append('.');
        }
        sb.append(WHITE).append(BOLD).append(parts[parts.length - 1]).append(RESET);
        return sb.toString();
    }
}