package cloud.thehsi.ExamplePlugin;

import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

public class Main implements Plugin {
    @Override
    public void onEnable() {
        logger.info("The Plugin has Started!");
    }

    @Override
    public void onDisable() {

    }

    @Override
    public String getName() {
        return "Example Plugin";
    }
}