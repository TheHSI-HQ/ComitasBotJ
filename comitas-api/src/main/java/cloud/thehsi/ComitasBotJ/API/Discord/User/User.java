package cloud.thehsi.ComitasBotJ.API.Discord.User;

import cloud.thehsi.ComitasBotJ.API.Bot.Comitas;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface User {
    /**
     * Get a user by their ID.
     *
     * @return The user.
     */
    @Nullable
    static User fromId(String id) {
        return Comitas.getUtilityBackend().getUserFromId(id);
    }
    /**
     * Get a user by their ID.
     *
     * @return The user.
     */
    @Nullable
    static User fromId(long id) {
        return Comitas.getUtilityBackend().getUserFromId(id);
    }

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
     * Determine if the user is a bot
     *
     * @return Is user a bot
     */
    boolean isBot();

    /**
     * Determine if the user is this bot
     *
     * @return Is user is this bot
     */
    boolean isMe();

    /**
     * Returns the users name.
     *
     * @return The users name
     */
    String getLoggableName();

    /**
     * Generates a Mention-String ({@code <@USERID>}).
     * <p>
     * Putting this String in any Discord Message, will mention this User.
     *
     * @return The generated Mention-Component
     */
    Component mention();

    /**
     * Messages this User with a Message of your choosing
     *
     * @param message The message to be sent
     * @return Was the message successfully send
     */
    @ApiStatus.Experimental
    MyMessage sendDirectMessage(Component message);

    /**
     * Messages this User with a Message of your choosing
     *
     * @param messageData The message data to be sent
     * @return Was the message successfully send
     */
    @ApiStatus.Experimental
    MyMessage sendDirectMessage(MessageData messageData);
}
