package cloud.thehsi.ComitasBotJ.API.Discord.Channel.Forum;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a tag in a forum channel
 */
public interface ForumTag {
    /**
     * Gets the tags name
     *
     * @return The tags name
     */
    @NotNull
    String getName();
}
