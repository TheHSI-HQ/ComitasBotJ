package cloud.thehsi.ComitasBasePlugin;

import cloud.thehsi.ComitasBasePlugin.ConsoleCommands.*;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

public class Main extends Plugin {
    @Override
    public void onLoad() {
        // Initialize Commands, on load, so the console is usable before the bot is connected
        new HelpConsoleCommand();
        new UpdateConsoleCommand();
        new VersionConsoleCommand();
        new PluginsConsoleCommand();
        new PermissionsConsoleCommand();
        new StopConsoleCommand();
        new ReloadConsoleCommand();
        new InviteConsoleCommand();
    }

    @Override
    public void onEnable() {
        getLogger().info("Base Loaded");
    }

    @Override
    public void onDisable() {
    }
}
