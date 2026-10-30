package cloud.thehsi.ComitasBotJ.API.Discord.Exceptions;

import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown, if the bot lacks a permission required for an action
 */
public class MissingBotPermissionException extends RuntimeException {
    private final @NotNull Permission permission;

    /**
     * @param permission The missing permission
     */
    public MissingBotPermissionException(@NotNull Permission permission) {
        super("The bot is missing the " + permission.name() + " permission");
        this.permission = permission;
    }

    /**
     * @return The permission the bot is missing
     */
    @NotNull
    public Permission getPermission() {
        return permission;
    }
}
