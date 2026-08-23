package cloud.thehsi.ComitasBotJ.API.Console;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

@SuppressWarnings({"unused", "ClassCanBeRecord"})
public class ConsoleColor {
    public @NotNull
    static final ConsoleColor RESET = new ConsoleColor("\033[0m");
    public @NotNull
    static final ConsoleColor BOLD = new ConsoleColor("\033[1m");

    // Standard foreground colors
    public @NotNull
    static final ConsoleColor BLACK = new ConsoleColor("\033[30m");
    public @NotNull
    static final ConsoleColor RED = new ConsoleColor("\033[31m");
    public @NotNull
    static final ConsoleColor GREEN = new ConsoleColor("\033[32m");
    public @NotNull
    static final ConsoleColor YELLOW = new ConsoleColor("\033[33m");
    public @NotNull
    static final ConsoleColor BLUE = new ConsoleColor("\033[34m");
    public @NotNull
    static final ConsoleColor MAGENTA = new ConsoleColor("\033[35m");
    public @NotNull
    static final ConsoleColor CYAN = new ConsoleColor("\033[36m");
    public @NotNull
    static final ConsoleColor WHITE = new ConsoleColor("\033[37m");

    // Bright foreground colors
    public @NotNull
    static final ConsoleColor BRIGHT_BLACK = new ConsoleColor("\033[90m");
    public @NotNull
    static final ConsoleColor BRIGHT_RED = new ConsoleColor("\033[91m");
    public @NotNull
    static final ConsoleColor BRIGHT_GREEN = new ConsoleColor("\033[92m");
    public @NotNull
    static final ConsoleColor BRIGHT_YELLOW = new ConsoleColor("\033[93m");
    public @NotNull
    static final ConsoleColor BRIGHT_BLUE = new ConsoleColor("\033[94m");
    public @NotNull
    static final ConsoleColor BRIGHT_MAGENTA = new ConsoleColor("\033[95m");
    public @NotNull
    static final ConsoleColor BRIGHT_CYAN = new ConsoleColor("\033[96m");
    public @NotNull
    static final ConsoleColor BRIGHT_WHITE = new ConsoleColor("\033[97m");

    // Background colors
    public @NotNull
    static final ConsoleColor BG_BLACK = new ConsoleColor("\033[40m");
    public @NotNull
    static final ConsoleColor BG_RED = new ConsoleColor("\033[41m");
    public @NotNull
    static final ConsoleColor BG_GREEN = new ConsoleColor("\033[42m");
    public @NotNull
    static final ConsoleColor BG_YELLOW = new ConsoleColor("\033[43m");
    public @NotNull
    static final ConsoleColor BG_BLUE = new ConsoleColor("\033[44m");
    public @NotNull
    static final ConsoleColor BG_MAGENTA = new ConsoleColor("\033[45m");
    public @NotNull
    static final ConsoleColor BG_CYAN = new ConsoleColor("\033[46m");
    public @NotNull
    static final ConsoleColor BG_WHITE = new ConsoleColor("\033[47m");

    // Bright background colors
    public @NotNull
    static final ConsoleColor BG_BRIGHT_BLACK = new ConsoleColor("\033[100m");
    public @NotNull
    static final ConsoleColor BG_BRIGHT_RED = new ConsoleColor("\033[101m");
    public @NotNull
    static final ConsoleColor BG_BRIGHT_GREEN = new ConsoleColor("\033[102m");
    public @NotNull
    static final ConsoleColor BG_BRIGHT_YELLOW = new ConsoleColor("\033[103m");
    public @NotNull
    static final ConsoleColor BG_BRIGHT_BLUE = new ConsoleColor("\033[104m");
    public @NotNull
    static final ConsoleColor BG_BRIGHT_MAGENTA = new ConsoleColor("\033[105m");
    public @NotNull
    static final ConsoleColor BG_BRIGHT_CYAN = new ConsoleColor("\033[106m");
    public @NotNull
    static final ConsoleColor BG_BRIGHT_WHITE = new ConsoleColor("\033[107m");
    private @NotNull
    final String value;

    private ConsoleColor(@NotNull String value) {
        this.value = value;
    }

    @NotNull
    public static ConsoleColor of(@NotNull Color color) {
        return new ConsoleColor("\u001B[38;2;" + color.getRed() + ";" + color.getGreen() + ";" + color.getBlue() + "m");
    }

    @Override
    @NotNull
    public String toString() {
        return value;
    }
}
