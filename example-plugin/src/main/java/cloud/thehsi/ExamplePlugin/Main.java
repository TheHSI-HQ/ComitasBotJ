package cloud.thehsi.ExamplePlugin;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.TextChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Attachment;
import cloud.thehsi.ComitasBotJ.API.Event.EventHandler;
import cloud.thehsi.ComitasBotJ.API.Event.Events.BotConnectEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageSentEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Listener;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

import java.util.List;
import java.util.concurrent.ExecutionException;

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
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onMessage(MessageSentEvent event) throws ExecutionException, InterruptedException {
        if (event.getAuthor().isMe()) return;

        List<Attachment> attachments = event.getMessage().getAttachments();

        for (Attachment attachment : attachments) {
            getLogger().info(attachment.getHash().get());
        }

        if (event.getRawContent().equals("!hello")) {
            event.reply("Hello " + event.getAuthor().mention());
        }
    }
}