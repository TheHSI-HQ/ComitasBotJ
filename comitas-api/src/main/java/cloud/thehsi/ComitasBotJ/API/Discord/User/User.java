package cloud.thehsi.ComitasBotJ.API.Discord.User;

import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("unused")
public class User {
    private final InternalUserImpl impl;

    public User(InternalUserImpl impl) {
        this.impl = impl;
    }

    /**
     * Returns the user's Username.
     *
     * @return The user's Username.
     */
    public String getUserName() {
        return impl.getUserName();
    }

    /**
     * Returns the user's Display Name.
     *
     * @return The user's Display Name
     */
    public String getDisplayName() {
        return impl.getDisplayName();
    }

    /**
     * Returns the user's ID.
     *
     * @return The user's ID
     */
    public Long getId() {
        return impl.getId();
    }

    /**
     * Determine if the message was sent by a bot
     *
     * @return Was message send by a bot
     */
    public boolean isBot() {
        return impl.isBot();
    }

    /**
     * Determine if the message was sent by this bot
     *
     * @return Was message send by this bot
     */
    public boolean isMe() {
        return impl.isMe();
    }

    /**
     * Generates a Mention-String ({@code <@USERID>}).
     * <p>
     * Putting this String in any Discord Message, will mention this User.
     * @return The generated Mention-String
     */
    public String mention() {
        return impl.mention();
    }

    /**
     * Messages this User with a Message of your choosing
     *
     * @param message The message you want to send
     * @return Was the message was successfully send
     */
    @ApiStatus.Experimental
    public boolean sendDirectMessage(String message) {
        return impl.sendDirectMessage(message);
    }
}
