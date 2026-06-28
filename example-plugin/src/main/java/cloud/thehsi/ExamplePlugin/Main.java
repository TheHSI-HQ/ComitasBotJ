package cloud.thehsi.ExamplePlugin;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
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

        Comitas.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Your code here

        getLogger().info("Bye Bye from ExamplePlugin!");
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onBotConnect(BotConnectEvent event) {
        for (Guild g : Comitas.getGuilds()) {
            for (Member m : g.getMembers()) {
                getLogger().info("Hi {}", m.getLoggableName());
            }
        }
        getLogger().info("Hello from {}", event.getUserName());
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onMessage(MessageEvent event) {
        if (event.getAuthor().isMe()) return;

        if (event.getRawContent().equals("!hello")) {
            event.reply("Hello " + event.getAuthor().mention());
        }
    }
}