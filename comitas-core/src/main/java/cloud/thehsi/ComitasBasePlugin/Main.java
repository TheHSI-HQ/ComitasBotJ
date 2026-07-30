package cloud.thehsi.ComitasBasePlugin;

import cloud.thehsi.ComitasBasePlugin.ConsoleCommands.*;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

public class Main extends Plugin {
    @Override
    public void onEnable() {
        getLogger().info("Base Loaded");

        // Initialize Commands
        new HelpConsoleCommand();
        new UpdateConsoleCommand();
        new VersionConsoleCommand();
        new PluginsConsoleCommand();
        new StopConsoleCommand();
        new ReloadConsoleCommand();
        new InviteConsoleCommand();
    }

    @Override
    public void onDisable() {
    }
}
