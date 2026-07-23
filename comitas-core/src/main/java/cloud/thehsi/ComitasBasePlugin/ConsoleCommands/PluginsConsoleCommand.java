package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleColor;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

import java.util.List;

public class PluginsConsoleCommand extends ConsoleCommand {
    public PluginsConsoleCommand(Plugin plugin) {
        plugin.createCommandBuilder(this)
                .setDescription("Lists all loaded Plugins")
                .addCommand("plugins")
                .addCommand("pl")
                .register();
    }

    @Override
    public void execute(String[] args) {
        getConsoleLogger().info("Plugins:");

        List<Plugin.PluginMetadata> plugins = Comitas.getPluginManager().getAllPluginMetadata();

        for (int i = 0; i < plugins.size(); i += 3) {
            String buf = " - " + ConsoleColor.BRIGHT_WHITE + plugins.get(i).name() + " " + ConsoleColor.BRIGHT_BLACK + "(" + plugins.get(i).version() + ")" + ConsoleColor.WHITE;
            if (plugins.size() > i + 1)
                buf += ", " + ConsoleColor.BRIGHT_WHITE + plugins.get(i + 1).name() + " " + ConsoleColor.BRIGHT_BLACK + "(" + plugins.get(i + 1).version() + ")" + ConsoleColor.WHITE;
            if (plugins.size() > i + 2)
                buf += ", " + ConsoleColor.BRIGHT_WHITE + plugins.get(i + 2).name() + " " + ConsoleColor.BRIGHT_BLACK + "(" + plugins.get(i + 2).version() + ")" + ConsoleColor.WHITE;
            if (plugins.size() > i + 3) buf += ",";
            getConsoleLogger().info(buf);
        }
    }
}
