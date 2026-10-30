package cloud.thehsi.ComitasBotJ.Utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsoleTextTest {
    @Test
    void plainRemovesEveryControlCharacter() {
        assertEquals("abcd", ConsoleText.plain("a\u001B[31mb\u0007c\r\nd"));
        assertEquals("FakePlugin", ConsoleText.plain("Fake Plugin‮"));
        assertEquals("", ConsoleText.plain(null));
    }

    @Test
    void forTerminalKeepsColorsAndLineBreaks() {
        assertEquals("\u001B[31mred\u001B[0m\nnext\tline", ConsoleText.forTerminal("\u001B[31mred\u001B[0m\nnext\tline"));
    }

    @Test
    void forTerminalRemovesOtherSequences() {
        // Clipboard write (OSC 52), screen clear and carriage return used to overwrite earlier output
        assertEquals("]52;c;aGk=text[2J", ConsoleText.forTerminal("\u001B]52;c;aGk=\u0007text\u001B[2J\r"));
    }
}
