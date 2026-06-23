package cloud.thehsi.ComitasBasePlugin.ConsoleCommands;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Console.ConsoleCommand;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

import java.util.List;

public class PluginsConsoleCommand extends ConsoleCommand {
    public PluginsConsoleCommand(Plugin plugin) {
        plugin.createCommandBuilder(this)
                .addCommand("plugins")
                .addCommand("pl")
                .register();
    }

    @Override
    public void execute(String[] args) {
        getConsoleLogger().info("Plugins:");

        List<String> plugins = Comitas.getPluginManager().getPluginNames();

        for (int i = 0; i < plugins.size(); i += 3) {
            String buf = " - " + plugins.get(i);
            if (plugins.size() > i + 1) buf += ", " + plugins.get(i);
            if (plugins.size() > i + 2) buf += ", " + plugins.get(i);
            if (plugins.size() > i + 3) buf += ",";
            getConsoleLogger().info(buf);
        }
    }
}
