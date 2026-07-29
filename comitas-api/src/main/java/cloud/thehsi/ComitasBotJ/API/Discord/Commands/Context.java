package cloud.thehsi.ComitasBotJ.API.Discord.Commands;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Guild.Guild;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Components.Component;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.Embeds.Embed;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import cloud.thehsi.ComitasBotJ.API.Discord.User.Member;

@SuppressWarnings("unused")
public interface Context {
    /**
     * The sender of the command
     *
     * @return The sender of the command
     */
    Member sender();

    /**
     * The channel in which the command was run
     *
     * @return The channel in which the command was run
     */
    MessageChannel channel();

    /**
     * The guild in which the command was run
     *
     * @return The guild in which the command was run
     */
    Guild guild();

    /**
     * The name of the command
     *
     * @return The name of the command
     */
    String commandName();

    /**
     * Reply to the interaction in the same channel this interaction was caused in
     *
     * @param message The message to be sent
     * @param ephemeral Should this message only be visible to the sender
     * @return The message that was sent
     */
    MyMessage reply(Component message, boolean ephemeral);

    /**
     * Reply to the interaction in the same channel this interaction was caused in with an Embed
     *
     * @param message The message to be sent
     * @param ephemeral Should this message only be visible to the sender
     * @param embed   The embed to attach to this message
     * @return The message that was sent
     */
    MyMessage reply(Component message, boolean ephemeral, Embed embed);

    /**
     * Reply to the interaction in the same channel this interaction was caused in with multiple Embeds
     *
     * @param message The message to be sent
     * @param ephemeral Should this message only be visible to the sender
     * @param embeds  The embeds to attach to this message
     * @return The message that was sent
     */
    MyMessage reply(Component message, boolean ephemeral, Embed... embeds);

    /**
     * Reply to the interaction in the same channel this interaction was caused in
     *
     * @param message The message to be sent
     * @return The message that was sent
     */
    MyMessage reply(Component message);

    /**
     * Reply to the interaction in the same channel this interaction was caused in with an Embed
     *
     * @param message The message to be sent
     * @param embed   The embed to attach to this message
     * @return The message that was sent
     */
    MyMessage reply(Component message, Embed embed);

    /**
     * Reply to the interaction in the same channel this interaction was caused in with multiple Embeds
     *
     * @param message The message to be sent
     * @param embeds  The embeds to attach to this message
     * @return The message that was sent
     */
    MyMessage reply(Component message, Embed... embeds);

    /**
     * Reply to the interaction, so only the sender can see
     *
     * @param message The message to be sent
     * @return The message that was sent
     */
    MyMessage replyEphemeral(Component message);

    /**
     * Reply to the interaction, so only the sender can see, with an Embed
     *
     * @param message The message to be sent
     * @param embed   The embed to attach to this message
     * @return The message that was sent
     */
    MyMessage replyEphemeral(Component message, Embed embed);

    /**
     * Reply to the interaction, so only the sender can see, with multiple Embeds
     *
     * @param message The message to be sent
     * @param embeds  The embeds to attach to this message
     * @return The message that was sent
     */
    MyMessage replyEphemeral(Component message, Embed... embeds);
}
