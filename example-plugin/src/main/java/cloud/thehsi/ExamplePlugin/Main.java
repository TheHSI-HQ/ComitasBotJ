package cloud.thehsi.ExamplePlugin;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Event.EventHandler;
import cloud.thehsi.ComitasBotJ.API.Event.Events.BotConnectEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageSentEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Listener;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataStorage;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataTypes;
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
        /*for (Guild g : Comitas.getGuilds()) {
            for (Channel c : g.getChannels()) {
                if (!(c instanceof TextChannel t)) continue;
                t.sendMessage("Test");
            }
        }*/
        getLogger().info("Hello from {}", event.getUserName());

        PersistentDataStorage storage = Comitas.getPluginManager().getPersistentDataStorage();
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onMessage(MessageSentEvent event) {
        if (event.getAuthor().isMe()) return;

        PersistentDataStorage storage = Comitas.getPluginManager().getPersistentDataStorage();

        if (event.getRawContent().startsWith("!hello")) {
            event.reply("Hello " + event.getAuthor().mention());
            event.reply("Also, last guy said: " + storage.get("message", PersistentDataTypes.STRING));
        }

        if (event.getRawContent().startsWith("!set")) {
            event.reply("Ok " + event.getAuthor().mention());
            storage.set("message", PersistentDataTypes.STRING, event.getRawContent());
        }
    }
}