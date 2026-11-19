package cloud.thehsi.ComitasBotJ.Discord;

import cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MissingBotPermissionException;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Translates JDA exceptions, which plugins can't reference (the sandbox hides JDA), into API exceptions.
 */
public final class DiscordExceptions {
    private DiscordExceptions() {
    }

    /**
     * @param throwable An exception thrown by an API call
     * @return The API equivalent, or the throwable itself if there is none
     */
    @NotNull
    public static Throwable translate(@NotNull Throwable throwable) {
        if (throwable instanceof InsufficientPermissionException e) {
            Permission permission = fromJda(e.getPermission());
            if (permission == null) return throwable;
            return new MissingBotPermissionException(permission,
                    "The bot is missing the " + permission.name() + " permission" + location(e), e);
        }
        return throwable;
    }

    /**
     * @return The API permission, or null if the API doesn't know it (yet)
     */
    @Nullable
    static Permission fromJda(@NotNull net.dv8tion.jda.api.Permission permission) {
        Permission[] permissions = Permission.fromLong(permission.getRawValue());
        return permissions.length == 1 ? permissions[0] : null;
    }

    @NotNull
    private static String location(@NotNull InsufficientPermissionException e) {
        JDA jda = DiscordAPI.nullableApi();
        Guild guild = jda == null ? null : e.getGuild(jda);
        GuildChannel channel = jda == null || e.getChannelId() == 0 ? null : e.getChannel(jda);

        String guildName = guild == null ? "guild " + e.getGuildId() : guild.getName() + " (" + e.getGuildId() + ")";
        if (e.getChannelId() == 0) return " in " + guildName;
        String channelName = channel == null ? "channel " + e.getChannelId() : "#" + channel.getName() + " (" + e.getChannelId() + ")";
        return " in " + channelName + " of " + guildName;
    }
}
