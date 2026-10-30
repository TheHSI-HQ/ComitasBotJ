package cloud.thehsi.ComitasBotJ.Discord;

import cloud.thehsi.ComitasBotJ.API.Discord.Exceptions.MissingBotPermissionException;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Checks the bot's own permissions before an action, so plugins get a clear exception instead of a Discord error.
 */
public final class BotPermissions {
    private BotPermissions() {
    }

    /**
     * @param guild   The guild the action happens in
     * @param channel The channel the permission is needed in, or null for a guild wide permission
     */
    public static void require(@NotNull Guild guild, @Nullable GuildChannel channel, @NotNull Permission permission) {
        net.dv8tion.jda.api.Permission[] needed = net.dv8tion.jda.api.Permission.getPermissions(Permission.asLong(permission))
                .toArray(net.dv8tion.jda.api.Permission[]::new);

        boolean granted = channel == null
                ? guild.getSelfMember().hasPermission(needed)
                : guild.getSelfMember().hasPermission(channel, needed);
        if (!granted) throw new MissingBotPermissionException(permission);
    }
}
