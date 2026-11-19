package cloud.thehsi.ComitasBotJ.Discord.Message;

import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.App.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Message.Components.ComponentParser;
import cloud.thehsi.ComitasBotJ.Sandbox.PluginDiscordPermissions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InternalMyMessage extends InternalMessage implements MyMessage {
    public InternalMyMessage(@NotNull net.dv8tion.jda.api.entities.Message message) {
        super(message);
    }

    public InternalMyMessage(@NotNull net.dv8tion.jda.api.entities.Message message, @Nullable Runnable deletionCallback) {
        super(message, deletionCallback);
    }

    @Override
    public void setContent(@NotNull Component content) {
        DebugLogging.action(content);
        String msg = ComponentParser.parseComponent(content);
        message.editMessage(msg).complete();
    }

    @Override
    public void setMessageData(@NotNull MessageData messageData) {
        DebugLogging.action(messageData);
        if (message.isFromGuild()) PluginDiscordPermissions.requireForContent(messageData);
        MessageDataParser.edit(message, messageData);
    }
}