package cloud.thehsi.ComitasBotJ.Discord;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.MissingPermissionException;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalChannel;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class PermissionManager {
    public static boolean missingPermissions(@NotNull Guild guild, @NotNull Permission... permissions) {
        if (!(guild instanceof InternalGuild(net.dv8tion.jda.api.entities.Guild iGuild)))
            return true;

        Role botRole = iGuild.getBotRole();
        if (botRole == null)
            return true;

        return !botRole.hasPermission(
                net.dv8tion.jda.api.Permission.getPermissions(
                        Permission.asLong(permissions)
                )
        );
    }

    public static boolean missingPermissions(@NotNull Guild guild, @NotNull Channel channel, @NotNull Permission... permissions) {
        if (!(guild instanceof InternalGuild(net.dv8tion.jda.api.entities.Guild iGuild)))
            return true;

        if (!(channel instanceof InternalChannel iChannel))
            return true;

        if (!(iChannel.channel instanceof GuildChannel guildChannel))
            return true;

        if (guildChannel.getGuild().getIdLong() != iGuild.getIdLong())
            return true;

        Role botRole = iGuild.getBotRole();
        if (botRole == null)
            return true;

        return !botRole.hasPermission(
                guildChannel,
                net.dv8tion.jda.api.Permission.getPermissions(
                        Permission.asLong(permissions)
                )
        );
    }

    public static void requirePermissions(@NotNull String methodName, @NotNull Guild guild, @NotNull Permission... permissions) throws MissingPermissionException {
        if (missingPermissions(guild, permissions))
            throw new MissingPermissionException("Missing permission(s) {" + String.join(", ", Arrays.stream(permissions).map(Enum::name).toArray(String[]::new)) + "} in guild '" + guild.getName() + "' to perform " + methodName);
    }

    public static void requirePermissions(@NotNull String methodName, @NotNull Guild guild, @NotNull Channel channel, @NotNull Permission... permissions) throws MissingPermissionException {
        if (missingPermissions(guild, channel, permissions))
            throw new MissingPermissionException("Missing permission(s) {" + String.join(", ", Arrays.stream(permissions).map(Enum::name).toArray(String[]::new)) + "} in guild '" + guild.getName() + "' and channel '" + channel.getName() + "' to perform " + methodName);
    }
}
