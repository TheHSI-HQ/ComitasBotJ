package cloud.thehsi.ComitasBotJ.Sandbox;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Replaces {@code System.out} / {@code System.err} in plugin code, every line is logged through the plugin's logger.
 */
final class PluginLogOutputStream extends OutputStream {
    private static final int MAX_LINE_LENGTH = 8192;

    private final @NotNull Logger logger;
    private final boolean error;
    private final @NotNull ByteArrayOutputStream line = new ByteArrayOutputStream();

    PluginLogOutputStream(@NotNull Logger logger, boolean error) {
        this.logger = logger;
        this.error = error;
    }

    @Override
    public synchronized void write(int b) {
        if (b == '\n') {
            flushLine();
            return;
        }
        line.write(b);
        if (line.size() >= MAX_LINE_LENGTH) flushLine();
    }

    @Override
    public synchronized void flush() {
        // PrintStream flushes after every println, a partial line is kept until it is complete
    }

    @Override
    public synchronized void close() {
        if (line.size() > 0) flushLine();
    }

    private void flushLine() {
        String text = line.toString(StandardCharsets.UTF_8);
        line.reset();
        if (text.endsWith("\r")) text = text.substring(0, text.length() - 1);
        if (error) logger.warn("{}", text);
        else logger.info("{}", text);
    }
}
