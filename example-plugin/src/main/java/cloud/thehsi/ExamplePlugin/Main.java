package cloud.thehsi.ExamplePlugin;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Style;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedAuthor;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedBuilder;
import cloud.thehsi.ComitasBotJ.API.Event.EventHandler;
import cloud.thehsi.ComitasBotJ.API.Event.Events.BotConnectEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageSentEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Listener;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataStorage;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataTypes;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

import java.awt.*;

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
        getLogger().info("Hello from {}", event.getUserName());

        PersistentDataStorage storage = Comitas.getPluginManager().getPersistentDataStorage();
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onMessage(MessageSentEvent event) {
        if (event.getAuthor().isMe()) return;

        PersistentDataStorage storage = Comitas.getPluginManager().getPersistentDataStorage();

        if (event.getRawContent().startsWith("!hello")) {
            Embed embed = new EmbedBuilder()
                    .setTitle("Hello " + event.getAuthor().getDisplayName())
                    .setDescription(
                            Component.text("Best Regards from " )
                                    .append(Component.text(Comitas.getBot().getDisplayName(), Style.BOLD, Style.UNDERLINE))
                    )
                    .setAuthor(new EmbedAuthor(Comitas.getBot().getDisplayName(), "https://www.thehsi.cloud/", "https://www.thehsi.cloud/logo.png"))
                    .setColor(new Color(151, 45, 231))
                    .build();

            event.reply(Component.empty(), embed);

            if (storage.has("message", PersistentDataTypes.STRING))
                event.reply(
                        Component.text("Also, ")
                                .append(Component.text("message", Style.CODE))
                                .append(Component.text(" is: "))
                                .append(Component.raw(storage.get("message", PersistentDataTypes.STRING)))
                );
        }

        if (event.getRawContent().startsWith("!set")) {
            String value = event.getRawContent().replaceFirst("!set ", "");

            Component c = Component.text("Set ", Style.ITALIC)
                    .append(Component.text("message", Style.CODE))
                    .append(Component.text(" to ", Style.ITALIC))
                    .append(Component.text(event.getRawContent().replaceFirst("!set ", ""), Style.CODE));

            event.reply(c);
            storage.set("message", PersistentDataTypes.STRING, value);
        }
    }
}