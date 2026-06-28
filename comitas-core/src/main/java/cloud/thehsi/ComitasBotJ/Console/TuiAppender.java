package cloud.thehsi.ComitasBotJ.Console;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Context;

public class TuiAppender extends AppenderBase<ILoggingEvent> {

    private static volatile ConsolePrompt instance;

    // ANSI color codes
    private static final String RESET = "\033[0m";
    private static final String GREY = "\033[90m"; // timestamp brackets
    private static final String WHITE = "\033[97m"; // logger name
    private static final String BOLD = "\033[1m";

    private static final String INFO = "\033[92m"; // bright green
    private static final String WARN = "\033[93m"; // bright yellow
    private static final String ERROR = "\033[91m"; // bright red
    private static final String DEBUG = "\033[94m"; // bright blue
    private static final String TRACE = "\033[90m"; // grey

    public TuiAppender(Context context) {
        setContext(context);
    }

    public static void setConsolePrompt(ConsolePrompt prompt) {
        instance = prompt;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (instance == null) return;

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
                + event.getFormattedMessage() + RESET;

        IThrowableProxy throwable = event.getThrowableProxy();
        if (throwable != null) {
            instance.appendLog(formatThrowable(throwable));
        }

        instance.appendLog(formatted);
    }

    private String formatThrowable(IThrowableProxy throwable) {
        StringBuilder sb = new StringBuilder();

        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss")
                .format(new java.util.Date());

        sb.append(GREY + "[" + RESET + DEBUG).append(timestamp).append(RESET).append(GREY).append("] ").append(ERROR);

        sb.append(throwable.getClassName())
                .append(": ")
                .append(throwable.getMessage())
                .append('\n');

        for (StackTraceElementProxy step : throwable.getStackTraceElementProxyArray()) {
            sb.append(GREY + "[" + RESET + DEBUG).append(timestamp).append(RESET).append(GREY).append("] ").append(ERROR);
            sb.append("\t")
                    .append(step.getSTEAsString())
                    .append('\n');
        }

        return sb.toString();
    }

    private String abbreviateLogger(String name) {
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