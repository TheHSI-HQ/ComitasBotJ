package cloud.thehsi.ComitasBotJ.Console;

import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandRegistry;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandTabCompleter;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleCompletionTest {
    private final InternalConsoleCommandRegistry registry = new InternalConsoleCommandRegistry();
    private final List<List<String>> receivedArgs = new ArrayList<>();

    private static final Plugin PLUGIN = new Plugin() {
        @Override
        public void onEnable() {
        }

        @Override
        public void onDisable() {
        }
    };

    private static final ConsoleCommandExecutor NOOP = new ConsoleCommandExecutor() {
        @Override
        public void execute(@NotNull String[] args, @NotNull String alias,
                            @NotNull ConsoleCommandRegistry.ConsoleCommand consoleCommand) {
        }
    };

    @BeforeEach
    void registerCommands() {
        ConsoleCommandTabCompleter completer = (args, alias, command) -> {
            receivedArgs.add(List.of(args));
            return args.length == 1 ? new String[]{"load", "unload", "\u001b[31mred", "load"} : new String[0];
        };
        registry.add(new ConsoleCommandRegistry.ConsoleCommand(List.of("plugins", "pl"), PLUGIN, "", NOOP, completer));
        registry.add(new ConsoleCommandRegistry.ConsoleCommand(List.of("stop"), PLUGIN, "", NOOP, null));
        registry.add(new ConsoleCommandRegistry.ConsoleCommand(List.of("broken"), PLUGIN, "", NOOP,
                (args, alias, command) -> {
                    throw new IllegalStateException("broken completer");
                }));
    }

    /**
     * Completes like the console does, the cursor is at the end of the line.
     */
    private List<String> complete(String line) {
        Parser parser = ConsolePrompt.createParser();
        return registry.complete(ConsolePrompt.wordsBeforeCursor(parser.parse(line, line.length(), Parser.ParseContext.COMPLETE)));
    }

    @Test
    void commandNamesAreOnlyCompletedForTheFirstWord() {
        assertEquals(List.of("plugins", "pl", "stop", "broken"), complete(""));
        assertEquals(List.of("plugins", "pl", "stop", "broken"), complete("pl"));
        assertFalse(complete("stop ").contains("plugins"));
        assertFalse(complete("plugins load ").contains("plugins"));
    }

    @Test
    void argumentsAreCompletedByTheCommandsCompleter() {
        assertEquals(List.of("load", "unload", "red"), complete("plugins "));
        assertEquals(List.of(List.of("")), receivedArgs);

        complete("pl  unl");
        assertEquals(List.of("unl"), receivedArgs.getLast());

        assertEquals(List.of(), complete("plugins load "));
        assertEquals(List.of("load", ""), receivedArgs.getLast());
    }

    @Test
    void commandsWithoutCompleterAndUnknownCommandsCompleteNothing() {
        assertEquals(List.of(), complete("stop "));
        assertEquals(List.of(), complete("unknown "));
    }

    @Test
    void failingCompletersCompleteNothing() {
        assertEquals(List.of(), complete("broken "));
    }

    @Test
    void quotesHaveNoMeaning() {
        complete("plugins \"it's ");
        assertEquals(List.of("\"it's", ""), receivedArgs.getLast());
    }
}
