package cloud.thehsi.ComitasBotJ.API.Console;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * A collection of default console colors, as well as a method to generate one from any {@link Color}
 */
@SuppressWarnings("unused")
public class ConsoleColor {
    /**
     * Reset Console Text Style
     */
    public @NotNull
    static final ConsoleColor RESET = new ConsoleColor("\033[0m");
    /**
     * Bold Console Text Style
     */
    public @NotNull
    static final ConsoleColor BOLD = new ConsoleColor("\033[1m");

    /**
     * Black Foreground Color
     */
    public @NotNull
    static final ConsoleColor BLACK = new ConsoleColor("\033[30m");
    /**
     * Red Foreground Color
     */
    public @NotNull
    static final ConsoleColor RED = new ConsoleColor("\033[31m");
    /**
     * Green Foreground Color
     */
    public @NotNull
    static final ConsoleColor GREEN = new ConsoleColor("\033[32m");
    /**
     * Yellow Foreground Color
     */
    public @NotNull
    static final ConsoleColor YELLOW = new ConsoleColor("\033[33m");
    /**
     * Blue Foreground Color
     */
    public @NotNull
    static final ConsoleColor BLUE = new ConsoleColor("\033[34m");
    /**
     * Magenta Foreground Color
     */
    public @NotNull
    static final ConsoleColor MAGENTA = new ConsoleColor("\033[35m");
    /**
     * Cyan Foreground Color
     */
    public @NotNull
    static final ConsoleColor CYAN = new ConsoleColor("\033[36m");
    /**
     * White Foreground Color
     */
    public @NotNull
    static final ConsoleColor WHITE = new ConsoleColor("\033[37m");

    /**
     * Bright Black Foreground Color
     */
    public @NotNull
    static final ConsoleColor BRIGHT_BLACK = new ConsoleColor("\033[90m");
    /**
     * Red Black Foreground Color
     */
    public @NotNull
    static final ConsoleColor BRIGHT_RED = new ConsoleColor("\033[91m");
    /**
     * Bright Green Foreground Color
     */
    public @NotNull
    static final ConsoleColor BRIGHT_GREEN = new ConsoleColor("\033[92m");
    /**
     * Bright Yellow Foreground Color
     */
    public @NotNull
    static final ConsoleColor BRIGHT_YELLOW = new ConsoleColor("\033[93m");
    /**
     * Bright Blue Foreground Color
     */
    public @NotNull
    static final ConsoleColor BRIGHT_BLUE = new ConsoleColor("\033[94m");
    /**
     * Bright Magenta Foreground Color
     */
    public @NotNull
    static final ConsoleColor BRIGHT_MAGENTA = new ConsoleColor("\033[95m");
    /**
     * Bright Cyan Foreground Color
     */
    public @NotNull
    static final ConsoleColor BRIGHT_CYAN = new ConsoleColor("\033[96m");
    /**
     * Bright White Foreground Color
     */
    public @NotNull
    static final ConsoleColor BRIGHT_WHITE = new ConsoleColor("\033[97m");

    /**
     * Black Background Color
     */
    public @NotNull
    static final ConsoleColor BG_BLACK = new ConsoleColor("\033[40m");
    /**
     * Red Background Color
     */
    public @NotNull
    static final ConsoleColor BG_RED = new ConsoleColor("\033[41m");
    /**
     * Green Background Color
     */
    public @NotNull
    static final ConsoleColor BG_GREEN = new ConsoleColor("\033[42m");
    /**
     * Yellow Background Color
     */
    public @NotNull
    static final ConsoleColor BG_YELLOW = new ConsoleColor("\033[43m");
    /**
     * Blue Background Color
     */
    public @NotNull
    static final ConsoleColor BG_BLUE = new ConsoleColor("\033[44m");
    /**
     * Magenta Background Color
     */
    public @NotNull
    static final ConsoleColor BG_MAGENTA = new ConsoleColor("\033[45m");
    /**
     * Cyan Background Color
     */
    public @NotNull
    static final ConsoleColor BG_CYAN = new ConsoleColor("\033[46m");
    /**
     * White Background Color
     */
    public @NotNull
    static final ConsoleColor BG_WHITE = new ConsoleColor("\033[47m");

    /**
     * Bright Black Background Color
     */
    public @NotNull
    static final ConsoleColor BG_BRIGHT_BLACK = new ConsoleColor("\033[100m");
    /**
     * Bright Red Background Color
     */
    public @NotNull
    static final ConsoleColor BG_BRIGHT_RED = new ConsoleColor("\033[101m");
    /**
     * Bright Green Background Color
     */
    public @NotNull
    static final ConsoleColor BG_BRIGHT_GREEN = new ConsoleColor("\033[102m");
    /**
     * Bright Yellow Background Color
     */
    public @NotNull
    static final ConsoleColor BG_BRIGHT_YELLOW = new ConsoleColor("\033[103m");
    /**
     * Bright Blue Background Color
     */
    public @NotNull
    static final ConsoleColor BG_BRIGHT_BLUE = new ConsoleColor("\033[104m");
    /**
     * Bright Magenta Background Color
     */
    public @NotNull
    static final ConsoleColor BG_BRIGHT_MAGENTA = new ConsoleColor("\033[105m");
    /**
     * Bright Cyan Background Color
     */
    public @NotNull
    static final ConsoleColor BG_BRIGHT_CYAN = new ConsoleColor("\033[106m");
    /**
     * Bright White Background Color
     */
    public @NotNull
    static final ConsoleColor BG_BRIGHT_WHITE = new ConsoleColor("\033[107m");
    private @NotNull
    final String value;

    private ConsoleColor(@NotNull String value) {
        this.value = value;
    }

    /**
     * Generate a {@link ConsoleColor} from a {@link Color}
     *
     * @param color The color for the console color
     * @return The generated console color
     */
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
