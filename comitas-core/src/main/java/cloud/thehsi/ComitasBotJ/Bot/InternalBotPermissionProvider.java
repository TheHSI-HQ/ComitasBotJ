package cloud.thehsi.ComitasBotJ.Bot;

import cloud.thehsi.ComitasBotJ.API.Bot.BotPermissionProvider;
import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import cloud.thehsi.ComitasBotJ.Discord.Channel.InternalChannel;
import cloud.thehsi.ComitasBotJ.Discord.Guild.InternalGuild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class InternalBotPermissionProvider implements BotPermissionProvider {
    public boolean has(@NotNull Guild guild, @NotNull Permission... permissions) {
        if (!(guild instanceof InternalGuild(net.dv8tion.jda.api.entities.Guild iGuild)))
            return false;

        Role botRole = iGuild.getBotRole();
        if (botRole == null)
            return false;

        return botRole.hasPermission(
                net.dv8tion.jda.api.Permission.getPermissions(
                        Permission.asLong(permissions)
                )
        );
    }

    @Override
    public boolean has(@NotNull Channel channel, @NotNull Permission... permissions) {
        if (!(channel instanceof InternalChannel iChannel))
            return false;

        if (!(iChannel.channel instanceof GuildChannel guildChannel))
            return false;

        Role botRole = guildChannel.getGuild().getBotRole();
        if (botRole == null)
            return false;

        return botRole.hasPermission(
                guildChannel,
                net.dv8tion.jda.api.Permission.getPermissions(
                        Permission.asLong(permissions)
                )
        );
    }

    @Override
    public @NotNull @Unmodifiable List<Permission> get(@NotNull Guild guild) {
        if (!(guild instanceof InternalGuild(net.dv8tion.jda.api.entities.Guild iGuild)))
            return List.of();

        Role botRole = iGuild.getBotRole();
        if (botRole == null)
            return List.of();

        long permissions = botRole.getPermissions().stream()
                .mapToLong(net.dv8tion.jda.api.Permission::getRawValue)
                .reduce(0L, (a, b) -> a | b);

        return List.of(Permission.fromLong(permissions));
    }

    @Override
    public @NotNull @Unmodifiable List<Permission> get(@NotNull Channel channel) {
        if (!(channel instanceof InternalChannel iChannel))
            return List.of();

        if (!(iChannel.channel instanceof GuildChannel guildChannel))
            return List.of();

        Role botRole = guildChannel.getGuild().getBotRole();
        if (botRole == null)
            return List.of();

        long permissions = botRole.getPermissions(guildChannel).stream()
                .mapToLong(net.dv8tion.jda.api.Permission::getRawValue)
                .reduce(0L, (a, b) -> a | b);

        return List.of(Permission.fromLong(permissions));
    }
}
