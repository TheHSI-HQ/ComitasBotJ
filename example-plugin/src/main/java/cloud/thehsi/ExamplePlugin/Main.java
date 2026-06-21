package cloud.thehsi.ExamplePlugin;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

public class Main extends Plugin {
    @Override
    public void onEnable() {
        // Your code Here

        getLogger().info("Hello World from Example Plugin");
    }

    @Override
    public void onDisable() {
        // Your code here

        getLogger().info("Bye Bye from ExamplePlugin!");
    }
}