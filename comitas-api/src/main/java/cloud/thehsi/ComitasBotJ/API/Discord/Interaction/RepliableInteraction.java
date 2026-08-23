package cloud.thehsi.ComitasBotJ.API.Discord.Interaction;

import cloud.thehsi.ComitasBotJ.API.Discord.InteractionAlreadyUsedException;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MessageData;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface RepliableInteraction extends InteractionContext {
    /**
     * Reply to the interaction in the same channel this interaction was caused in
     *
     * @param message   The message to be sent
     * @param ephemeral Should this message only be visible to the sender
     * @return The message that was sent
     */
    @NotNull
    MyMessage reply(@NotNull Component message, boolean ephemeral) throws InteractionAlreadyUsedException;

    /**
     * Reply to the interaction in the same channel this interaction was caused in with multiple Embeds
     *
     * @param messageData The message data to be sent
     * @param ephemeral   Should this message only be visible to the sender
     * @return The message that was sent
     */
    @NotNull
    MyMessage reply(@NotNull MessageData messageData, boolean ephemeral) throws InteractionAlreadyUsedException;

    /**
     * Reply to the interaction in the same channel this interaction was caused in
     *
     * @param message The message to be sent
     * @return The message that was sent
     */
    @NotNull
    MyMessage reply(@NotNull Component message) throws InteractionAlreadyUsedException;

    /**
     * Reply to the interaction in the same channel this interaction was caused in
     *
     * @param messageData The message data to be sent
     * @return The message that was sent
     */
    @NotNull
    MyMessage reply(@NotNull MessageData messageData) throws InteractionAlreadyUsedException;

    /**
     * Reply to the interaction, so only the sender can see
     *
     * @param message The message to be sent
     * @return The message that was sent
     */
    @NotNull
    MyMessage replyEphemeral(@NotNull Component message) throws InteractionAlreadyUsedException;

    /**
     * Reply to the interaction, so only the sender can see
     *
     * @param messageData The message data to be sent
     * @return The message that was sent
     */
    @NotNull
    MyMessage replyEphemeral(@NotNull MessageData messageData) throws InteractionAlreadyUsedException;
}