package cloud.thehsi.ComitasBasePlugin;

import cloud.thehsi.ComitasBasePlugin.ConsoleCommands.*;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

public class Main extends Plugin {
    @Override
    public void onEnable() {
        getLogger().info("Base Loaded");

        // Initialize Commands
        new HelpConsoleCommand(this);
        new VersionConsoleCommand(this);
        new PluginsConsoleCommand(this);
        new StopConsoleCommand(this);
        new ReloadConsoleCommand(this);
    }

    @Override
    public void onDisable() {
    }
}
