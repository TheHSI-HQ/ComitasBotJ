package cloud.thehsi.ComitasBotJ.API.Discord.User;

import cloud.thehsi.ComitasBotJ.API.Discord.Permission;
import org.jetbrains.annotations.ApiStatus;

import java.awt.*;
import java.util.List;

@SuppressWarnings("unused")
public interface User {
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
    Color getPrimaryColor();

    /**
     * Returns the user's secondary color.
     *
     * @return The user's secondary color
     */
    Color getSecondaryColor();

    /**
     * Returns the user's tertiary color.
     *
     * @return The user's tertiary color
     */
    Color getTertiaryColor();

    /**
     * Generates a list of all user permissions
     *
     * @return The generated list of permissions
     */
    List<Permission> getPermissions();

    /**
     * Messages this User with a Message of your choosing
     *
     * @param message The message you want to send
     * @return Was the message was successfully send
     */
    @ApiStatus.Experimental
    boolean sendDirectMessage(String message);
}
