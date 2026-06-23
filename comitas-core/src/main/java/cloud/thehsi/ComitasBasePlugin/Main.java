package cloud.thehsi.ComitasBasePlugin;

import cloud.thehsi.ComitasBasePlugin.ConsoleCommands.HelpConsoleCommand;
import cloud.thehsi.ComitasBasePlugin.ConsoleCommands.PluginsConsoleCommand;
import cloud.thehsi.ComitasBasePlugin.ConsoleCommands.ReloadConsoleCommand;
import cloud.thehsi.ComitasBasePlugin.ConsoleCommands.StopConsoleCommand;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

public class Main extends Plugin {
    @Override
    public void onEnable() {
        getLogger().info("Base Loaded");

        // Initialize Commands
        new HelpConsoleCommand(this);
        new PluginsConsoleCommand(this);
        new StopConsoleCommand(this);
        new ReloadConsoleCommand(this);
    }

    @Override
    public void onDisable() {
    }
}
