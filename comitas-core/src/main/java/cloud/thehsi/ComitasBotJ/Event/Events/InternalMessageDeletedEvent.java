package cloud.thehsi.ComitasBotJ.Event.Events;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Event.Events.MessageDeletedEvent;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalMessageChannel;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InternalMessageDeletedEvent implements MessageDeletedEvent {
    private @NotNull
    final MessageChannel channel;
    private @Nullable
    final Guild guild;
    private final long id;

    public InternalMessageDeletedEvent(@NotNull net.dv8tion.jda.api.events.message.MessageDeleteEvent event) {
        this.channel = new InternalMessageChannel(event.getChannel());
        if (event.isFromGuild())
            this.guild = new InternalGuild(event.getGuild());
        else
            this.guild = null;
        this.id = event.getMessageIdLong();
    }

    @Override
    public long getId() {
        DebugLogging.action();
        return id;
    }

    @Override
    public @NotNull MessageChannel getChannel() {
        DebugLogging.action();
        return channel;
    }

    @Override
    @Nullable
    public Guild getGuild() {
        DebugLogging.action();
        return guild;
    }
}
