package cloud.thehsi.ComitasBotJ.API.Discord.User;

import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public interface Member {
    /**
     * Returns the user's Username.
     *
     * @return The user's Username.
     */
    String getUserName();

    /**
     * Returns the user's Display Name.
     *
     * @return The user's Display Name
     */
    String getDisplayName();

    /**
     * Returns the user's ID.
     *
     * @return The user's ID
     */
    Long getId();

    /**
     * Determine if the message was sent by a bot
     *
     * @return Was message send by a bot
     */
    boolean isBot();

    /**
     * Determine if the message was sent by this bot
     *
     * @return Was message send by this bot
     */
    boolean isMe();

    /**
     * Generates a Mention-String ({@code <@USERID>}).
     * <p>
     * Putting this String in any Discord Message, will mention this User.
     * @return The generated Mention-String
     */
    String mention();

    /**
     * Returns the user's primary color.
     *
     * @return The user's primary color
     */
    @Nullable
    Color getPrimaryColor();

    /**
     * Returns the user's secondary color.
     *
     * @return The user's secondary color
     */
    @Nullable
    Color getSecondaryColor();

    /**
     * Returns the user's tertiary color.
     *
     * @return The user's tertiary color
     */
    @Nullable
    Color getTertiaryColor();

    /**
     * Returns the user's name in there color.
     *
     * @return The user's name with there color
     */
    String getLoggableName();

    /**
     * Generates a list of all user permissions
     *
     * @return The generated list of permissions
     */
    List<Permission> getPermissions();

    /**
     * Kick this user
     *
     * @return Was the user successfully kicked
     */
    CompletableFuture<Boolean> kick();

    /**
     * Kick this user
     *
     * @param reason The kick reason
     * @return Was the user successfully kicked
     */
    CompletableFuture<Boolean> kick(String reason);

    /**
     * Ban this user
     *
     * @return Was the user successfully banned
     */
    CompletableFuture<Boolean> ban();

    /**
     * Ban this user
     *
     * @param reason The ban reason
     * @return Was the user successfully banned
     */
    CompletableFuture<Boolean> ban(String reason);

    /**
     * Ban and delete the last {@code deletionPeriodHours} hours of message from this user
     *
     * @param deletionPeriodHours The amount of hours of messages to delete alongside the ban
     * @return Was the user successfully banned
     */
    CompletableFuture<Boolean> ban(int deletionPeriodHours);

    /**
     * Ban and delete the last {@code deletionPeriodHours} hours of message from this user
     *
     * @param reason The ban reason
     * @param deletionPeriodHours The amount of hours of messages to delete alongside the ban
     * @return Was the user successfully banned
     */
    CompletableFuture<Boolean> ban(String reason, int deletionPeriodHours);

    /**
     * Messages this User with a Message of your choosing
     *
     * @param message The message you want to send
     * @return Was the message successfully send
     */
    @ApiStatus.Experimental
    boolean sendDirectMessage(String message);
}
