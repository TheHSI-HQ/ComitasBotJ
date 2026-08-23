package cloud.thehsi.ComitasBotJ.API.Bot;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.Channel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface BotPermissionProvider {
    /**
     * Verify the bot has all the listed permissions in the provided guild, and only then return true
     *
     * @param guild       The guild to check in
     * @param permissions The permissions to validate
     * @return DOes the bot have the requested permissions in the provided guild
     */
    boolean has(@NotNull Guild guild, @NotNull Permission... permissions);

    /**
     * Verify the bot has all the listed permissions in the provided channel, and only then return true
     *
     * @param channel     The channel to check in
     * @param permissions The permissions to validate
     * @return DOes the bot have the requested permissions in the provided channel
     */
    boolean has(@NotNull Channel channel, @NotNull Permission... permissions);

    /**
     * Lists all permissions the bot has in the provided guild
     *
     * @param guild The guild to check in
     * @return The list of guild specific bot permissions
     */
    @NotNull
    @Unmodifiable
    List<Permission> get(@NotNull Guild guild);

    /**
     * Lists all permissions the bot has in the provided cannel
     *
     * @param channel The cannel to check in
     * @return The list of cannel specific bot permissions
     */
    @NotNull
    @Unmodifiable
    List<Permission> get(@NotNull Channel channel);
}
