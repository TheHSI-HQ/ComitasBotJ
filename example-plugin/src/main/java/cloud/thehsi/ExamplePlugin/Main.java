package cloud.thehsi.ExamplePlugin;

import cloud.thehsi.ComitasBotJ.API.Bot.Bot;
import cloud.thehsi.ComitasBotJ.API.Event.EventHandler;
import cloud.thehsi.ComitasBotJ.API.Event.Events.BotConnectEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Listener;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

public class Main extends Plugin implements Listener {
    @Override
    public void onEnable() {
        // Your code Here

        getLogger().info("Hello World from Example Plugin");

        Bot.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Your code here

        getLogger().info("Bye Bye from ExamplePlugin!");
    }

    @EventHandler
    public void onBotConnect(BotConnectEvent event) {
        getLogger().info("Hello from {}", event.getUsername());
    }

    @EventHandler
    public void onMessage(MessageEvent event) {
        if (event.getRawContent().equals("!ping")) {
            event.replyToMessage(
                    event.getAuthor().getDisplayName() + "\n" + event.getAuthor().getUsername()
            );
            event.replyToMessage("Pong!");
        }
    }
}