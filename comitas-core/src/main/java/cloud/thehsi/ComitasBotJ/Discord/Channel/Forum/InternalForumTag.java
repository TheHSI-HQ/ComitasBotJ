package cloud.thehsi.ComitasBotJ.Discord.Channel.Forum;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum.ForumTag;
import cloud.thehsi.ComitasBotJ.DebugLogging;
import org.jetbrains.annotations.NotNull;

public record InternalForumTag(net.dv8tion.jda.api.entities.channel.forums.ForumTag tag,
                               long channelId) implements ForumTag {
    @Override
    public @NotNull String getName() {
        DebugLogging.action();
        return tag.getName();
    }
}
