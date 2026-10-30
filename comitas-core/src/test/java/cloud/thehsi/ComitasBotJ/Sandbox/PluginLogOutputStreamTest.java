package cloud.thehsi.ComitasBotJ.Sandbox;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PluginLogOutputStreamTest {
    private static List<String> log(String text) {
        Logger logger = (Logger) LoggerFactory.getLogger("Plugin.Test." + System.nanoTime());
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        PluginLogOutputStream stream = new PluginLogOutputStream(logger, false);
        new PrintStream(stream, true, StandardCharsets.UTF_8).print(text);
        stream.close();

        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

    @Test
    void splitsBuffersIntoLines() {
        assertEquals(List.of("first", "second", "", "partial"), log("first\r\nsecond\n\npartial"));
    }

    @Test
    void splitsOverlongLines() {
        List<String> lines = log("x".repeat(8192 + 10) + "\n");
        assertEquals(2, lines.size());
        assertEquals(8192, lines.get(0).length());
        assertEquals(10, lines.get(1).length());
    }
}
