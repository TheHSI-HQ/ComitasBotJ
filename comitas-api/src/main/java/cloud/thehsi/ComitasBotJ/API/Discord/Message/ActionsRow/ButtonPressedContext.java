package cloud.thehsi.ComitasBotJ.API.Discord.Message.ActionsRow;

import cloud.thehsi.ComitasBotJ.API.Discord.Channel.MessageChannel;
import cloud.thehsi.ComitasBotJ.API.Discord.Interaction.RepliableInteraction;
import cloud.thehsi.ComitasBotJ.API.Discord.Message.MyMessage;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface ButtonPressedContext extends RepliableInteraction {
    /**
     * Get the channel in which the button was pressed in
     *
     * @return The channel where the button was pressed
     */
    @NotNull
    MessageChannel getChannel();

    /**
     * Get the id of the button
     *
     * @return The button's id
     */
    @NotNull
    String getButtonId();

    /**
     * Get the message the clicked button is attached to
     *
     * @return The clicked buttons message
     */
    @NotNull
    MyMessage getMessage();
}
