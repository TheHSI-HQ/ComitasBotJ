package cloud.thehsi.ComitasBotJ.Discord;

import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The guild wide permissions the bot has, taken once when plugins are enabled (on start and every reload).
 *
 * @param guilds The permissions per guild, keyed by a readable guild name. A guild, in which the bot is an
 *               administrator, contains every permission.
 */
public record BotPermissionSnapshot(@NotNull @Unmodifiable Map<String, Set<Permission>> guilds) {
    /**
     * @param jda The connected bot
     * @return The bot's current permissions in every guild it is in
     */
    @NotNull
    public static BotPermissionSnapshot capture(@NotNull JDA jda) {
        Map<String, Set<Permission>> guilds = new LinkedHashMap<>();
        for (Guild guild : jda.getGuilds()) {
            long raw = net.dv8tion.jda.api.Permission.getRaw(guild.getSelfMember().getPermissions());
            Set<Permission> permissions = EnumSet.noneOf(Permission.class);
            permissions.addAll(List.of(Permission.fromLong(raw)));
            if (permissions.contains(Permission.ADMINISTRATOR))
                permissions.addAll(List.of(Permission.values()));
            guilds.put(guild.getName() + " (" + guild.getId() + ")", Collections.unmodifiableSet(permissions));
        }
        return new BotPermissionSnapshot(Collections.unmodifiableMap(guilds));
    }

    /**
     * @param permission The permission to look for
     * @return The guilds, in which the bot lacks the permission
     */
    @NotNull
    public List<String> guildsLacking(@NotNull Permission permission) {
        return guilds.entrySet().stream()
                .filter(entry -> !entry.getValue().contains(permission))
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * @param permission The permission to look for
     * @return Does the bot have the permission in at least one guild
     */
    public boolean grantedAnywhere(@NotNull Permission permission) {
        return guilds.values().stream().anyMatch(permissions -> permissions.contains(permission));
    }
}
