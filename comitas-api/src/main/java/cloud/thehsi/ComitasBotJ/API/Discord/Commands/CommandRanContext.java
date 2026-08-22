package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.InteractionAlreadyUsedException;
import cloud.thehsi.ComitasBotJ.API.Discord.InteractionContext;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface CommandRanContext extends InteractionContext {
    /**
     * The sender of the command
     *
     * @return The sender of the command
     */
    @Nullable
    Member getSender();

    /**
     * The channel in which the command was run
     *
     * @return The channel in which the command was run
     */
    @Override
    MessageChannel getChannel();

    /**
     * The name of the command
     *
     * @return The name of the command
     */
    String getCommandName();

    /**
     * Reply to the interaction in the same channel this interaction was caused in
     *
     * @param message   The message to be sent
     * @param ephemeral Should this message only be visible to the sender
     * @return The message that was sent
     */
    MyMessage reply(Component message, boolean ephemeral) throws InteractionAlreadyUsedException;

    /**
     * Reply to the interaction in the same channel this interaction was caused in with multiple Embeds
     *
     * @param messageData The message data to be sent
     * @param ephemeral   Should this message only be visible to the sender
     * @return The message that was sent
     */
    MyMessage reply(MessageData messageData, boolean ephemeral) throws InteractionAlreadyUsedException;

    /**
     * Reply to the interaction in the same channel this interaction was caused in
     *
     * @param message The message to be sent
     * @return The message that was sent
     */
    MyMessage reply(Component message) throws InteractionAlreadyUsedException;

    /**
     * Reply to the interaction in the same channel this interaction was caused in
     *
     * @param messageData The message data to be sent
     * @return The message that was sent
     */
    MyMessage reply(MessageData messageData) throws InteractionAlreadyUsedException;

    /**
     * Reply to the interaction, so only the sender can see
     *
     * @param message The message to be sent
     * @return The message that was sent
     */
    MyMessage replyEphemeral(Component message) throws InteractionAlreadyUsedException;

    /**
     * Reply to the interaction, so only the sender can see
     *
     * @param messageData The message data to be sent
     * @return The message that was sent
     */
    MyMessage replyEphemeral(MessageData messageData) throws InteractionAlreadyUsedException;
}
