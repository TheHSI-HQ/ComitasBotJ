package cloud.thehsi.ComitasBotJ.API.Discord.Exceptions;

import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * @param permission The missing permission
     * @param message    The detail message, e.g. naming the guild or channel the permission is missing in
     * @param cause      The underlying exception, or null
     */
    public MissingBotPermissionException(@NotNull Permission permission, @NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
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
