package cloud.thehsi.ComitasBotJ.Discord.Channel.Thread;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Thread.ThreadTag;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import org.jetbrains.annotations.NotNull;

public record InternalThreadTag(ForumTag tag, long channelId) implements ThreadTag {
    @Override
    public @NotNull String getName() {
        DebugLogging.action();
        return tag.getName();
    }
}
