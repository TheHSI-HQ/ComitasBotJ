package cloud.thehsi.ExamplePlugin;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Emoji.Emojis.Emojis;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Style;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedAuthor;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.EmbedBuilder;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Event.EventHandler;
import cloud.thehsi.ComitasBotJ.API.Event.EventPriority;
import cloud.thehsi.ComitasBotJ.API.Event.Events.BotReadyEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageReceivedEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserChangeGuildDisplayNameEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Events.UserJoinGuildEvent;
import cloud.thehsi.ComitasBotJ.API.Event.Listener;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataStorage;
import cloud.thehsi.ComitasBotJ.API.Plugin.PersistentData.PersistentDataTypes;
import cloud.thehsi.ComitasBotJ.API.Plugin.Plugin;

import java.awt.*;
import java.util.Objects;

public class Main extends Plugin implements Listener {
    @Override
    public void onEnable() {
        getLogger().info("Hello World from Example Plugin");

        Comitas.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    private void test2(UserChangeGuildDisplayNameEvent event) {
        if (Objects.requireNonNullElse(event.getNewDisplayName(), "").startsWith("!"))
            event.undo();
    }

    @Override
    public void onDisable() {
        getLogger().info("Bye Bye from ExamplePlugin!");
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOW)
    public void onBotConnect(BotReadyEvent event) {
        getLogger().info("Hello from {}", event.getUserName());

        Comitas.getPluginManager().getPersistentDataStorage();

        Comitas.getCommandRegistry().register(new ExampleCommand());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onUserJoinGuildEvent(UserJoinGuildEvent event) {
        event.getGuild().getDefaultChannel().sendMessage(
                Emojis.WAVE().asComponent()
                        .append(" Hello ")
                        .append(event.getMember().mention())
                        .append(", welcome to ")
                        .append(event.getGuild().getName())
                        .append("!")
        );
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOW)
    public void onMessage(MessageReceivedEvent event) {
        if (Comitas.getBot().isMeOrNull(event.getAuthor())) return;

        if (event.getRawContent().startsWith("!hello")) {
            PersistentDataStorage storage = Comitas.getPluginManager().getPersistentDataStorage();

            Embed embed = new EmbedBuilder()
                    .setTitle("Hello " + event.getAuthor().getDisplayName())
                    .setDescription(
                            Component.text("Best Regards from " )
                                    .append(Component.text(Comitas.getBot().getDisplayName(), Style.BOLD, Style.UNDERLINE))
                                    .append(" ")
                                    .append(Emojis.THUMBSUP())
                    )
                    .setAuthor(new EmbedAuthor(Comitas.getBot().getDisplayName(), "https://www.thehsi.cloud/", "https://www.thehsi.cloud/logo.png"))
                    .setColor(new Color(151, 45, 231))
                    .build();

            event.reply(new MessageData(embed));

            if (storage.has("message", PersistentDataTypes.STRING))
                event.reply(
                        Component.text("Also, ")
                                .append(Component.text("message", Style.CODE))
                                .append(Component.text(" is: "))
                                .append(Component.raw(storage.get("message", PersistentDataTypes.STRING)))
                );
        }

        if (event.getRawContent().startsWith("!set")) {
            PersistentDataStorage storage = Comitas.getPluginManager().getPersistentDataStorage();
            String value = event.getRawContent().replaceFirst("!set ", "");

            Component c = Component.text("Set ", Style.ITALIC)
                    .append(Component.text("message", Style.CODE))
                    .append(Component.text(" to ", Style.ITALIC))
                    .append(Component.text(value, Style.CODE));

            event.reply(c);
            storage.set("message", PersistentDataTypes.STRING, value);
        }
    }
}