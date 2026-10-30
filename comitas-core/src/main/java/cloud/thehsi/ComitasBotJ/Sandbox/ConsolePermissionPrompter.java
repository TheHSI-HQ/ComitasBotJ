package cloud.thehsi.ComitasBotJ.Sandbox;

import cloud.thehsi.ComitasBotJ.Console.ConsolePrompt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;

import java.io.PrintWriter;

/**
 * Asks for plugin permissions on the JLine console.
 * <p>
 * Plugins are loaded before the console thread starts reading, or from a console command,
 * which runs on the console thread itself, so reading here doesn't compete with the console prompt.
 */
public final class ConsolePermissionPrompter implements PermissionPrompter {
    private final @NotNull ConsolePrompt consolePrompt;

    public ConsolePermissionPrompter(@NotNull ConsolePrompt consolePrompt) {
        this.consolePrompt = consolePrompt;
    }

    @Override
    @Nullable
    public synchronized String ask(@NotNull String message, @NotNull String prompt) {
        if (System.console() == null) return null;

        LineReader reader = consolePrompt.lineReader();
        try {
            PrintWriter writer = reader.getTerminal().writer();
            writer.println(message);
            writer.flush();
            return reader.readLine(prompt);
        } catch (UserInterruptException e) {
            return "n";
        } catch (EndOfFileException | IllegalStateException e) {
            // IllegalStateException: another thread is already reading (e.g. reload triggered by a plugin)
            return null;
        }
    }
}
