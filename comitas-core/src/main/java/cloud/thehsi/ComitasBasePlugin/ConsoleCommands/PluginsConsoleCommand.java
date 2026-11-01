package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommandExecutor;
import cloud.thehsi.ComitasBotJ.Plugin.InternalPluginManager;
import cloud.thehsi.ComitasBotJ.Plugin.PluginLister;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;

/**
 * <pre>
 * plugins                          Lists the loaded plugins
 * plugins -a                       Also lists unloaded, disabled and errored plugins, with the reason why
 * plugins load &lt;jar&gt;               Loads a jar of the plugins folder
 * plugins unload &lt;uuid/jar&gt;        Unloads a plugin, it stays unloaded on reloads until it is loaded again
 * plugins reload &lt;uuid/jar&gt;        Reloads a single plugin, e.g. after its jar was replaced
 * </pre>
 */
public class PluginsConsoleCommand extends ConsoleCommandExecutor {
    private static final String USAGE = "plugins [-a] | plugins load <jar> | plugins unload <uuid/jar> | plugins reload <uuid/jar>";

    public PluginsConsoleCommand() {
        Comitas.getConsoleCommandRegistry().register(
                this,
                "Lists, loads, unloads and reloads plugins. Usage: " + USAGE,
                "plugins", "pl"
        );
    }

    @Override
    public void execute(@NotNull String[] args) {
        if (args.length == 0) {
            PluginLister.listPlugins(false);
            return;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        // Jar names may contain spaces
        String target = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).strip();

        switch (action) {
            case "-a", "--all", "all" -> {
                if (args.length != 1) usage();
                else PluginLister.listPlugins(true);
            }
            case "load", "unload", "reload" -> {
                if (target.isEmpty()) {
                    usage();
                    return;
                }
                InternalPluginManager pluginManager = (InternalPluginManager) Comitas.getPluginManager();
                switch (action) {
                    case "load" -> pluginManager.loadPlugin(target);
                    case "unload" -> pluginManager.unloadPlugin(target);
                    default -> pluginManager.reloadPlugin(target);
                }
            }
            default -> usage();
        }
    }

    private void usage() {
        getConsoleLogger().error("Usage: {}", USAGE);
    }
}
